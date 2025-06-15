package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.extensions.*
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.PsiDependentFileContent
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.PyStubElementTypes
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyStringLiteralUtil

private const val MODEL_NAME_PROP = "_name"
private const val INHERIT_NAME_PROP = "_inherit"
private const val INHERITS_NAME_PROP = "_inherits"

private const val FIELD_PREFIX = "fields."
private const val MANY_2_ONE = "Many2one"
private const val MANY_2_ONE_FIELD_DECL = FIELD_PREFIX + MANY_2_ONE

private const val DELEGATE = "delegate"
private const val COMODEL_NAME = "comodel_name"

private val WANTED_PROPS = setOf(MODEL_NAME_PROP, INHERIT_NAME_PROP, INHERITS_NAME_PROP)

class OdooModelNameIndexer: DataIndexer<String, List<OdooModelNameIndexItem>, FileContent> {
    var addonPaths: List<String>? = null

    override fun map(fileContent: FileContent): MutableMap<String, List<OdooModelNameIndexItem>> = (fileContent as? PsiDependentFileContent)?.lighterAST?.let { lighterAST ->
        if (addonPaths == null) {
            addonPaths = fileContent.project.addonPaths
        }

        lighterAST.getModuleNameAndRelativePathInModule(fileContent, addonPaths!!)
                ?.let { (moduleName, filePath) ->
                    lighterAST.getAllModelNamesWithItems(fileContent.contentAsText, moduleName, filePath).toMutableMap()
                }
                ?: mutableMapOf()
    } ?: mutableMapOf()
}

private fun LighterAST.getModuleNameAndRelativePathInModule(fileContent: PsiDependentFileContent,
                                                    addonPaths: List<String>): Pair<String, String>? {
    if (addonPaths.isEmpty()) return run {
        logger<OdooModelNameIndexer>().warn("Could not find module name for ${fileContent.fileName} while indexing")
        null
    }

    val virtualFile = fileContent.psiFile.virtualFile
    return virtualFile.getContainingModuleNameAndRelativePathInModule(addonPaths)
}

private fun getModelData(fileContent: CharSequence, lighterAST: LighterAST, stmtList: LighterASTNode, moduleName: String, filePath: String): Pair<String, OdooModelNameIndexItem>? {
    var modelName: String? = null
    var modelNameOffset = 0
    var parents: List<NameLocation> = emptyList()
    val inheritsDelegatedFields = mutableMapOf<String, String>() // map of field name to comodel name
    val statements = lighterAST.getChildrenOfType(stmtList, PyElementTypes.ASSIGNMENT_STATEMENT)

    val modelVariables = getModelVariables(lighterAST, fileContent, statements, WANTED_PROPS)

    modelVariables[MODEL_NAME_PROP]?.let { modelNameStmt ->
        lighterAST.useFirstChild(modelNameStmt, PyElementTypes.STRING_LITERAL_EXPRESSION) {
            modelName = PyStringLiteralUtil.getStringValue(fileContent.buildString(it))
            modelNameOffset = it.startOffset
        }
    }

    modelVariables[INHERIT_NAME_PROP]?.let { inheritStmt ->
        val listLitExpr = lighterAST.firstChild(inheritStmt, PyElementTypes.LIST_LITERAL_EXPRESSION)

        if (listLitExpr != null) {
            parents = lighterAST.mapChildren(listLitExpr, PyElementTypes.STRING_LITERAL_EXPRESSION) { strLitExpr ->
                val name = PyStringLiteralUtil.getStringValue(fileContent.buildString(strLitExpr))
                val offset = strLitExpr.startOffset

                NameLocation(name, offset)
            }

            if (modelName == null) parents.firstOrNull()?.let { (name, offset) ->
                modelName = name
                modelNameOffset = offset
            }
        } else {
            val strLitExpr = lighterAST.firstChild(inheritStmt, PyElementTypes.STRING_LITERAL_EXPRESSION)
            if (strLitExpr != null) {
                val name = PyStringLiteralUtil.getStringValue(fileContent.buildString(strLitExpr))
                val offset = strLitExpr.startOffset

                if (modelName == null) {
                    modelName = name
                    modelNameOffset = offset
                    parents = listOf(NameLocation(name, offset))
                } else {
                    parents = listOf(NameLocation(name, offset))
                }
            }
        }
    }

    // There is no point in looking for the other info if
    // we didn't find the modelName by now
    if (modelName == null) return null

    modelVariables[INHERITS_NAME_PROP]?.let { inheritsStmt ->
        lighterAST.useSingleChild(inheritsStmt, PyElementTypes.DICT_LITERAL_EXPRESSION) { dictLitExpr ->
            lighterAST.forEachChild(dictLitExpr, PyElementTypes.KEY_VALUE_EXPRESSION) { keyValExpr ->
                lighterAST.getChildrenOfType(keyValExpr, PyElementTypes.STRING_LITERAL_EXPRESSION)
                        .takeIf { it.size == 2 }
                        ?.map { literal -> PyStringLiteralUtil.getStringValue(fileContent.buildString(literal)) }
                        ?.let { (keyLit, valueLit) ->
                            // we want fieldName -> comodelName map
                            // in odoo _inherits is a comodelName -> fieldName map
                            inheritsDelegatedFields[valueLit] = keyLit
                        }
            }
        }
    }

    val methods = getMethods(lighterAST, fileContent, stmtList)
    val fields = getFields(lighterAST, fileContent, stmtList)
    val indexItem = OdooModelNameIndexItem(
            filePath = filePath,
            modelNameOffset = modelNameOffset,
            moduleName = moduleName,
            parents = parents,
            methods = methods,
            fields = fields,
            delegateMap = inheritsDelegatedFields,
    )

    return modelName!! to indexItem
}

/**
 * @param wantedVariables list of names to look for, if null the function looks for all the possible names
 */
private fun getModelVariables(lighterAST: LighterAST,
                              fileContent: CharSequence,
                              assignmentStmts: List<LighterASTNode>,
                              wantedVariables: Set<String>? = null): Map<String, LighterASTNode> = buildMap {
    val remaining = wantedVariables?.toMutableSet()
    for (stmt in assignmentStmts) {
        // if the assignment statement does not start with a '_' it can never be a model variable
        if (fileContent[stmt.startOffset] != '_') continue

        val targetName = lighterAST.useFirstChild(stmt, PyStubElementTypes.TARGET_EXPRESSION, action = fileContent::buildString)
                ?: continue

        if (remaining != null && targetName in remaining) {
            remaining.remove(targetName)
            put(targetName, stmt)

            if (remaining.isEmpty()) break
        } else if (remaining == null) {
            put(targetName, stmt)
        }
    }
}

private fun getMethods(lighterAST: LighterAST, fileContent: CharSequence, stmtList: LighterASTNode): List<NameLocation> =
        lighterAST.mapChildrenNotNull(stmtList, PyElementTypes.FUNCTION_DECLARATION) { functionDeclaration ->
            lighterAST.useSingleChild(functionDeclaration, PyTokenTypes.IDENTIFIER) { identifier ->
                val name = fileContent.buildString(identifier)
                NameLocation(name, identifier.startOffset)
            }
        }

private fun getFields(lighterAST: LighterAST, fileContent: CharSequence, stmtList: LighterASTNode): List<FieldInfo> =
        lighterAST.mapChildrenNotNull(stmtList, PyElementTypes.ASSIGNMENT_STATEMENT) { assignmentStmt ->
            lighterAST.useSingleChild(assignmentStmt, PyElementTypes.CALL_EXPRESSION) { callExpr ->
                lighterAST.useSingleChild(callExpr, PyElementTypes.REFERENCE_EXPRESSION) { refExpr ->
                    val fieldsString = fileContent.buildString(refExpr)

                    if (fieldsString == MANY_2_ONE_FIELD_DECL) {
                        // Check for potential delegate field
                        getMany2oneField(lighterAST, fileContent, assignmentStmt, callExpr)
                    } else if (fieldsString.startsWith(FIELD_PREFIX)) {
                        // Field declaration found
                        getAnyField(lighterAST, fileContent, assignmentStmt)
                    } else null
                }
            }
        }

private fun getMany2oneField(lighterAST: LighterAST, fileContent: CharSequence, assignmentStmt: LighterASTNode, callExpr: LighterASTNode) =
        lighterAST.useSingleChild(callExpr, PyElementTypes.ARGUMENT_LIST) { argumentList ->
            val keywordArgumentExpressions = lighterAST
                    .getChildrenOfType(argumentList, PyElementTypes.KEYWORD_ARGUMENT_EXPRESSION)

            val isDelegateField = keywordArgumentExpressions.any { keywordArgExpr ->
                DELEGATE == lighterAST.useSingleChild(keywordArgExpr, PyTokenTypes.IDENTIFIER) { identifier ->
                    fileContent.buildString(identifier)
                }
            }

            val comodelKeywordArgExpr = keywordArgumentExpressions.firstOrNull { keywordArgExpr ->
                COMODEL_NAME == lighterAST.useSingleChild(keywordArgExpr, PyTokenTypes.IDENTIFIER) { identifier ->
                    fileContent.buildString(identifier)
                }
            }

            val comodelStringLiteral = if (comodelKeywordArgExpr != null) {
                lighterAST.getSingleChild(comodelKeywordArgExpr, PyElementTypes.STRING_LITERAL_EXPRESSION)
            } else {
                // take the first string literal as comodel_name
                lighterAST.firstChild(argumentList, PyElementTypes.STRING_LITERAL_EXPRESSION)
            }

            comodelStringLiteral?.let {
                val comodelName = PyStringLiteralUtil.getStringValue(fileContent.buildString(it))

                lighterAST.useSingleChild(assignmentStmt, PyElementTypes.TARGET_EXPRESSION) { targetExpr ->
                    val fieldName = fileContent.buildString(targetExpr)
                    FieldInfo.Many2OneField(fieldName, targetExpr.startOffset, comodelName, isDelegateField)
                }
            }
        }

private fun getAnyField(lighterAST: LighterAST, fileContent: CharSequence, assignmentStmt: LighterASTNode) =
        lighterAST.useSingleChild(assignmentStmt, PyElementTypes.TARGET_EXPRESSION) { targetExpr ->
            val fieldName = fileContent.buildString(targetExpr)
            FieldInfo.AnyField(fieldName, targetExpr.startOffset)
        }

private fun LighterAST.getAllModelNamesWithItems(
    fileContent: CharSequence,
    moduleName: String,
    filePath: String,
): Map<String, List<OdooModelNameIndexItem>> = mapChildrenNotNull(root, PyStubElementTypes.CLASS_DECLARATION) { classNode ->
        useSingleChild(classNode, PyElementTypes.STATEMENT_LIST) { stmtList ->
            getModelData(fileContent, this, stmtList, moduleName, filePath)
        }
    }
    .groupBy { it.first }
    .mapValues { (_, list) -> list.map { it.second } }
    .toMap()

private fun CharSequence.buildString(node: LighterASTNode, startOffset: Int = node.startOffset, endOffset: Int = node.endOffset): String {
    val content = this
    return buildString { append(content, startOffset, endOffset) }
}