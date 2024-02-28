package com.github.allepilli.odoodevelopmentplugin.references.python.model_inherit_values

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression

class ModelInheritValuesReferenceProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<PsiReference> =
            if (psiElement is PyStringLiteralExpression) arrayOf(
                    ModelNameReference(psiElement, TextRange.allOf(psiElement.stringValue).shiftRight(1))
            ) else emptyArray()
}