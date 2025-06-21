package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_env

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.psi.PsiReference
import com.intellij.psi.util.childrenOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil

class ModelInEnvValuesReferenceProvider: PyStringLiteralReferenceProvider() {
    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> =
            element.parent
                    .childrenOfType<PyReferenceExpression>()
                    .singleOrNull()
                    ?.text
                    ?.takeIf { it == "self.env" }
                    ?.let { _ -> listOf(ModelNameReference(element, PyStringLiteralUtil.getContentRange(element.text))) }
                    ?: emptyList()
}