package com.github.allepilli.odoodevelopmentplugin.references.python.model_inherit_values

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil

class ModelInheritValuesReferenceProvider : PyStringLiteralReferenceProvider() {
    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> =
            listOf(ModelNameReference(element, PyStringLiteralUtil.getContentRange(element.text), canReferenceContainingClass = false))
}