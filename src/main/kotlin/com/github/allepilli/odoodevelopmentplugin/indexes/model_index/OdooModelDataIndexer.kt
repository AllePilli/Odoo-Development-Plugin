package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.getChildrenOfType
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.PsiDependentFileContent
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.PyElementTypesFacade
import com.jetbrains.python.psi.PyStringLiteralUtil

class OdooModelDataIndexer: DataIndexer<String, Void?, FileContent> {
    override fun map(fileContent: FileContent): MutableMap<String, Void?> = (fileContent as? PsiDependentFileContent)
            ?.lighterAST
            ?.getAllModelNames(fileContent.contentAsText)
            ?.associateWith { null }
            ?.toMutableMap()
            ?: mutableMapOf()
}

private fun LighterAST.getAllModelNames(fileContent: CharSequence): List<String> =
        getChildrenOfType(root, PyElementTypesFacade.INSTANCE.classDeclaration).mapNotNull { classNode ->
            getChildrenOfType(classNode, PyElementTypes.STATEMENT_LIST).singleOrNull()?.let { stmtList ->
                val statements = getChildrenOfType(stmtList, PyElementTypes.ASSIGNMENT_STATEMENT)
                val targetNames = statements.map { stmt ->
                    getChildrenOfType(stmt, PyElementTypes.TARGET_EXPRESSION).firstOrNull()?.let {
                        buildString { append(fileContent, it.startOffset, it.endOffset) }
                    }
                }

                if ("_name" in targetNames) {
                    val stmt = statements[targetNames.indexOf("_name")]
                    getChildrenOfType(stmt, PyElementTypes.STRING_LITERAL_EXPRESSION).firstOrNull()?.let {
                        PyStringLiteralUtil.getStringValue(buildString {
                            append(fileContent, it.startOffset, it.endOffset)
                        })
                    }
                } else if ("_inherit" in targetNames) {
                    val stmt = statements[targetNames.indexOf("_inherit")]
                    var name: String? = null

                    object : RecursiveLighterASTNodeWalkingVisitor(this) {
                        override fun visitNode(element: LighterASTNode) {
                            // The name will be the first occurrence of a String literal in the value of the '_inherit' property
                            if (element.tokenType == PyElementTypes.STRING_LITERAL_EXPRESSION) {
                                name = PyStringLiteralUtil.getStringValue(buildString {
                                    append(fileContent, element.startOffset, element.endOffset)
                                })
                                stopWalking()
                            }

                            super.visitNode(element)
                        }
                    }.visitNode(stmt)

                    name
                } else null
            }
        }
