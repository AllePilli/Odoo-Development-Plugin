package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
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
            ?.let { it.getAllModelNames(fileContent.contentAsText) + it.getAllModelInheritValues(fileContent.contentAsText) }
            ?.associateWith { null }
            ?.toMutableMap()
            ?: mutableMapOf()
}

private fun LighterAST.getAllModelInheritValues(fileContent: CharSequence): List<String> =
        getChildrenOfType(root, PyElementTypesFacade.INSTANCE.classDeclaration).flatMapNotNull { classNode ->
            getChildrenOfType(classNode, PyElementTypes.STATEMENT_LIST).singleOrNull()?.let { stmtList ->
                buildList {
                    object : RecursiveLighterASTNodeWalkingVisitor(this@getAllModelInheritValues) {
                        var inAss = false
                        var inNameAss = false
                        var inInheritList = false

                        override fun visitNode(element: LighterASTNode) {
                            if (element.tokenType == PyElementTypes.ASSIGNMENT_STATEMENT) {
                                if (inInheritList) stopWalking()
                                else {
                                    inAss = true
                                    inNameAss = false
                                }
                            } else if (inAss && element.tokenType == PyElementTypes.TARGET_EXPRESSION) {
                                inNameAss = "_inherit" == buildString {
                                    append(fileContent, element.startOffset, element.endOffset)
                                }
                            } else if (inNameAss && !inInheritList) {
                                if (element.tokenType == PyElementTypes.STRING_LITERAL_EXPRESSION) {
                                    add(PyStringLiteralUtil.getStringValue(buildString {
                                        append(fileContent, element.startOffset, element.endOffset)
                                    }))
                                    stopWalking()
                                } else if (element.tokenType == PyElementTypes.LIST_LITERAL_EXPRESSION) {
                                    inInheritList = true
                                }
                            } else if (inNameAss && element.tokenType == PyElementTypes.STRING_LITERAL_EXPRESSION) {
                                add(PyStringLiteralUtil.getStringValue(buildString {
                                    append(fileContent, element.startOffset, element.endOffset)
                                }))
                            }

                            super.visitNode(element)
                        }
                    }.visitNode(stmtList)
                }
            }
        }

private fun LighterAST.getAllModelNames(fileContent: CharSequence): List<String> =
        getChildrenOfType(root, PyElementTypesFacade.INSTANCE.classDeclaration).mapNotNull { classNode ->
            getChildrenOfType(classNode, PyElementTypes.STATEMENT_LIST).singleOrNull()?.let { stmtList ->
                var name: String? = null

                object: RecursiveLighterASTNodeWalkingVisitor(this) {
                    var inAssignment = false
                    var inNameAssignment = false

                    override fun visitNode(element: LighterASTNode) {
                        if (element.tokenType == PyElementTypes.ASSIGNMENT_STATEMENT) {
                            inAssignment = true
                            inNameAssignment = false
                        } else if (inAssignment && element.tokenType == PyElementTypes.TARGET_EXPRESSION) {
                            inNameAssignment = "_name" == buildString {
                                append(fileContent, element.startOffset, element.endOffset)
                            }
                        } else if (inNameAssignment && element.tokenType == PyElementTypes.STRING_LITERAL_EXPRESSION) {
                            name = PyStringLiteralUtil.getStringValue(buildString {
                                append(fileContent, element.startOffset, element.endOffset)
                            })
                            stopWalking()
                        }

                        super.visitNode(element)
                    }
                }.visitNode(stmtList)

                name
            }
        }
