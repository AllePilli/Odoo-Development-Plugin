package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_constrains_wrapper

import com.github.allepilli.odoodevelopmentplugin.references.SimpleFieldNameReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralUtil

class FieldsInConstrainsWrapperReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val fieldName = PyStringLiteralUtil.getStringValue(element.text)
        return arrayOf(SimpleFieldNameReference(element, TextRange.allOf(fieldName).shiftRight(1)))
    }
}
