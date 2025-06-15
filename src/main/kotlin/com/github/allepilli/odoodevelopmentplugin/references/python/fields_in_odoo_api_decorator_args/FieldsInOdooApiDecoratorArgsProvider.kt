package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_odoo_api_decorator_args

import com.github.allepilli.odoodevelopmentplugin.references.SimpleFieldNameReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralUtil

class FieldsInOdooApiDecoratorArgsProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val fieldName = PyStringLiteralUtil.getStringValue(element.text)

        val textRange = if ('.' in fieldName) {
            val dotIdx = fieldName.indexOfFirst { it == '.' }
            TextRange.create(1, dotIdx + 1)
        } else TextRange.allOf(fieldName).shiftRight(1)

        return arrayOf(SimpleFieldNameReference(element, textRange))
    }
}