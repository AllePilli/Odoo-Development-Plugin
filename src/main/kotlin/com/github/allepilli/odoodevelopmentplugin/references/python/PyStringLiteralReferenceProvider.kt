package com.github.allepilli.odoodevelopmentplugin.references.python

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil

/**
 * [PsiReferenceProvider] specifically for references inside [PyStringLiteralExpression]s.
 * Adds some error prevention for strings with unbalanced quotes.
 */
abstract class PyStringLiteralReferenceProvider: PsiReferenceProvider() {
    final override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
            if (element !is PyStringLiteralExpression || !PyStringLiteralUtil.isQuoted(element.text)) emptyArray()
            else getReferences(element, context).toTypedArray()

    abstract fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference>
}