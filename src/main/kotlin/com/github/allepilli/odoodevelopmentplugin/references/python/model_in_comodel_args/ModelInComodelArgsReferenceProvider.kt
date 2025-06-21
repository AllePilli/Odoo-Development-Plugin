package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_comodel_args

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.psi.PsiReference
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.FunctionParameter
import com.jetbrains.python.psi.*

class ModelInComodelArgsReferenceProvider: PyStringLiteralReferenceProvider() {
    private val classes = setOf("Many2one", "One2many", "Many2many")

    private fun getCoModelParameter(isKeywordArgument: Boolean) = object : FunctionParameter {
        override fun getPosition(): Int = 0
        override fun getName(): String? = if (isKeywordArgument) "comodel_name" else null
    }

    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> {
        val argumentList = element.parentOfType<PyArgumentList>() ?: return emptyList()
        val parameterExpression = argumentList.getValueExpressionForParam(getCoModelParameter(element.parent is PyKeywordArgument))
                ?: return emptyList()

        if (parameterExpression != element) return emptyList()

        val callExpression = argumentList.parent as? PyCallExpression ?: return emptyList()
        val referenceExpression = callExpression.childrenOfType<PyReferenceExpression>()
                .firstOrNull()
                ?: return emptyList()

        if (referenceExpression.name in classes) {
            return listOf(ModelNameReference(element, PyStringLiteralUtil.getContentRange(element.text)))
        }

        return emptyList()
    }
}