package com.github.allepilli.odoodevelopmentplugin

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.endOffset
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.*

val XmlFile.isOdooDataFile: Boolean
    get() = rootTag?.name in Constants.ODOO_DATA_FILE_ROOTS

val XmlElement.inOdooDataFile: Boolean
    get() = (containingFile as? XmlFile)?.isOdooDataFile == true

fun LighterAST.getChildrenOfType(node: LighterASTNode, type: IElementType): List<LighterASTNode>
    = LightTreeUtil.getChildrenOfType(this, node, type)

fun LighterAST.getChildrenOfType(node: LighterASTNode, tokenSet: TokenSet): List<LighterASTNode> =
        LightTreeUtil.getChildrenOfType(this, node, tokenSet)

inline fun <T, R> Iterable<T>.flatMapNotNull(transform: (T) -> Iterable<R>?): List<R> = mapNotNull(transform).flatten()

fun <T: PsiFile?, Self: PsiFilePattern<T, Self>> PsiFilePattern<T, Self>.withFileName(fileName: String, ) =
        with(object : PatternCondition<T>("withFileName") {
            override fun accepts(file: T & Any, context: ProcessingContext?): Boolean =
                    file.containingFile.name == fileName
        })

val VirtualFile.directories: List<VirtualFile>
    get() {
        if (!isDirectory) throw IllegalStateException("VirtualFile ${this.name} is not a directory")

        return children.filter { it.isDirectory }
    }

/**
 * Inserts an element into a sequence (list, dict, set, tuple) literal.
 * ! MULTILINE DOES NOT WORK FOR LISTS !
 * @param toInsert the element to insert
 * @param afterThis [toInsert] gets inserted after this element, if null, the element gets inserted at the end of the sequence
 * @param addRedundantCommaOnMultiline adds a redundant comma after the inserted element if it is the last element of the sequence, only when the sequence is a multiline sequence
 * @return the actual inserted element
 */
fun PySequenceExpression.insertElement(toInsert: PyExpression, afterThis: PyExpression? = null, addRedundantCommaOnMultiline: Boolean = true): PsiElement {
    val documentManager = PsiDocumentManager.getInstance(project)

    val isMultiline = documentManager.getDocument(containingFile)?.let { document ->
            document.getLineNumber(textOffset) != document.getLineNumber(textOffset + textLength)
        } ?: false


    val element = if (elements.isEmpty()) {
        // insert before the end of the sequence
        val closingToken = findElementAt(endOffset)!!
        val element = addBefore(toInsert, closingToken)

        if (addRedundantCommaOnMultiline && isMultiline) {
            addBefore(PyElementGenerator.getInstance(project).createComma().psi, element)
        }

        element
    } else {
        val _afterThis = afterThis ?: elements.last()
        val gen = PyElementGenerator.getInstance(project)

        // insert after _afterThis in the sequence
        if (addRedundantCommaOnMultiline && isMultiline)
            gen.insertItemIntoList(this, _afterThis, toInsert)
        else
            gen.insertItemIntoListRemoveRedundantCommas(this, _afterThis, toInsert)
    }

    if (isMultiline && this !is PyListLiteralExpression) {
        // Because you can only add PyExpressions to a PyListLiteralExpression

        val newLine = PyElementGenerator.getInstance(project)
                .createFromText(LanguageLevel.forElement(this), PsiWhiteSpace::class.java, " \n ")

        addBefore(newLine, element)
    }

    return element
}