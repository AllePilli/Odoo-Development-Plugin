package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.getChildrenOfType
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.PsiDependentFileContent
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.PyStubElementTypes
import com.jetbrains.python.psi.PyStringLiteralUtil

class OdooModelNameIndexer: DataIndexer<String, OdooModelNameIndexItem, FileContent> {
    override fun map(fileContent: FileContent): MutableMap<String, OdooModelNameIndexItem> = (fileContent as? PsiDependentFileContent)?.lighterAST?.let { lighterAST ->
        lighterAST.getAllModelNamesWithItems(fileContent.contentAsText).toMutableMap()
    } ?: mutableMapOf()
}

private fun getModelNameData(fileContent: CharSequence, lighterAST: LighterAST, statements: List<LighterASTNode>): Pair<String, OdooModelNameIndexItem>? {
    var modelName: String? = null
    var modelNameOffset: Int = 0
    var parents: List<Pair<String, Int>> = emptyList()

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

                    name to offset
                }

                if (modelName == null) parents.firstOrNull()?.let { (name, offset) ->
                    modelName = name
                    modelNameOffset = offset
                } else {
                    /* when we have already found a _name stmt and then encounter an _inherit stmt, we have all
                       the necessary information and can safely stop looking. Note we can not do this for the other
                       branch of the if statement because a _name stmt could in theory follow an _inherit stmt  */
                    return modelName!! to OdooModelNameIndexItem(
                            modelNameOffset = modelNameOffset,
                            parents = parents,
                    )
                }
            } else {
                lighterAST.getChildrenOfType(stmt, PyElementTypes.STRING_LITERAL_EXPRESSION).firstOrNull()?.let { strLitExpr ->
                    val name = PyStringLiteralUtil.getStringValue(buildString {
                        append(fileContent, strLitExpr.startOffset, strLitExpr.endOffset)
                    })
                    val offset = strLitExpr.startOffset

                    if (modelName == null) {
                        modelName = name
                        modelNameOffset = offset
                    } else return modelName!! to OdooModelNameIndexItem(
                            modelNameOffset = modelNameOffset,
                            parents = listOf(name to offset),
                    )
                }
            }
        }
    }

    return if (modelName != null) {
        modelName!! to OdooModelNameIndexItem(
                modelNameOffset = modelNameOffset,
                parents = parents,
        )
    } else null
}

private fun LighterAST.getAllModelNamesWithItems(fileContent: CharSequence): Map<String, OdooModelNameIndexItem> =
        getChildrenOfType(root, PyStubElementTypes.CLASS_DECLARATION).mapNotNull { classNode ->
            getChildrenOfType(classNode, PyElementTypes.STATEMENT_LIST).singleOrNull()?.let { stmtList ->
                val statements = getChildrenOfType(stmtList, PyElementTypes.ASSIGNMENT_STATEMENT)

                getModelNameData(fileContent, this, statements)
            }
        }.toMap()