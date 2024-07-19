package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModuleName
import com.github.allepilli.odoodevelopmentplugin.extensions.slowGetContainingModuleName
import com.github.allepilli.odoodevelopmentplugin.getChildrenOfType
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

class OdooModelNameIndexer: DataIndexer<String, OdooModelNameIndexItem, FileContent> {
    var addonPaths: List<String>? = null

    override fun map(fileContent: FileContent): MutableMap<String, OdooModelNameIndexItem> = (fileContent as? PsiDependentFileContent)?.lighterAST?.let { lighterAST ->
        if (addonPaths == null) {
            addonPaths = fileContent.project.addonPaths
        }

        val moduleName = lighterAST.getModuleName(fileContent, addonPaths!!)
        lighterAST.getAllModelNamesWithItems(fileContent.contentAsText, moduleName).toMutableMap()
    } ?: mutableMapOf()
}

private fun LighterAST.getModuleName(fileContent: PsiDependentFileContent, addonPaths: List<String>): String? {
    return if (addonPaths.isEmpty()) {
        fileContent.psiFile.originalFile.slowGetContainingModuleName() ?: run {
            logger<OdooModelNameIndexer>().warn("Could not find module name for ${fileContent.fileName} while indexing")
            null
        }
    } else {
        fileContent.psiFile.virtualFile.getContainingModuleName(addonPaths)
    }
}

private fun getModelNameData(fileContent: CharSequence, lighterAST: LighterAST, classNode: LighterASTNode, stmtList: LighterASTNode, moduleName: String?): Pair<String, OdooModelNameIndexItem>? {
    var modelName: String? = null
    var modelNameOffset: Int = 0
    var parents: List<NameLocation> = emptyList()
    val statements = lighterAST.getChildrenOfType(stmtList, PyElementTypes.ASSIGNMENT_STATEMENT)

    for (stmt in statements) {
        // if the assignment statement does not start with a '_' it can never be '_name' or '_inherit'
        if (fileContent[stmt.startOffset] != '_') continue

        val targetName = lighterAST.getChildrenOfType(stmt, PyStubElementTypes.TARGET_EXPRESSION).firstOrNull()?.let {
            buildString { append(fileContent, it.startOffset, it.endOffset) }
        }

        if (targetName == "_name") lighterAST.getChildrenOfType(stmt, PyElementTypes.STRING_LITERAL_EXPRESSION).firstOrNull()?.let {
            modelName = PyStringLiteralUtil.getStringValue(buildString {
                append(fileContent, it.startOffset, it.endOffset)
            })
            modelNameOffset = it.startOffset
        } else if (targetName == "_inherit") {
            val listLitExpr = lighterAST.getChildrenOfType(stmt, PyElementTypes.LIST_LITERAL_EXPRESSION).firstOrNull()

            if (listLitExpr != null) {
                parents = lighterAST.getChildrenOfType(listLitExpr, PyElementTypes.STRING_LITERAL_EXPRESSION).map { strLitExpr ->
                    val name = PyStringLiteralUtil.getStringValue(buildString {
                        append(fileContent, strLitExpr.startOffset, strLitExpr.endOffset)
                    })
                    val offset = strLitExpr.startOffset

                    NameLocation(name, offset)
                }

                if (modelName == null) parents.firstOrNull()?.let { (name, offset) ->
                    modelName = name
                    modelNameOffset = offset
                } else {
                    /* when we have already found a _name stmt and then encounter an _inherit stmt, we have all
                       the necessary information and can safely stop looking. Note we can not do this for the other
                       branch of the if statement because a _name stmt could in theory follow an _inherit stmt  */
                    break
                }
            } else {
                val strLitExpr = lighterAST.getChildrenOfType(stmt, PyElementTypes.STRING_LITERAL_EXPRESSION).firstOrNull()
                if (strLitExpr != null) {
                    val name = PyStringLiteralUtil.getStringValue(buildString {
                        append(fileContent, strLitExpr.startOffset, strLitExpr.endOffset)
                    })
                    val offset = strLitExpr.startOffset

                    if (modelName == null) {
                        modelName = name
                        modelNameOffset = offset
                        parents = listOf(NameLocation(name, offset))
                    } else {
                        parents = listOf(NameLocation(name, offset))
                        break
                    }
                }
            }
        }
    }

    val methods = if (modelName != null) {
        lighterAST.getChildrenOfType(stmtList, PyElementTypes.FUNCTION_DECLARATION).mapNotNull { functionDeclaration ->
            lighterAST.getChildrenOfType(functionDeclaration, PyTokenTypes.IDENTIFIER).singleOrNull()?.let { identifier ->
                val name = buildString { append(fileContent, identifier.startOffset, identifier.endOffset) }
                NameLocation(name, identifier.startOffset)
            }
        }
    } else emptyList()

    return if (modelName != null) {
        modelName!! to OdooModelNameIndexItem(
                modelNameOffset = modelNameOffset,
                moduleName = moduleName,
                parents = parents,
                methods = methods,
        )
    } else null
}


private fun LighterAST.getAllModelNamesWithItems(fileContent: CharSequence, moduleName: String?): Map<String, OdooModelNameIndexItem> =
        getChildrenOfType(root, PyStubElementTypes.CLASS_DECLARATION).mapNotNull { classNode ->
            getChildrenOfType(classNode, PyElementTypes.STATEMENT_LIST).singleOrNull()?.let { stmtList ->
                getModelNameData(fileContent, this, classNode, stmtList, moduleName)
            }
        }.toMap()