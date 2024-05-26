package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_comodel_args

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.FunctionParameter
import com.jetbrains.python.psi.*

class ModelInComodelArgsReferenceProvider: PsiReferenceProvider() {
    private val classes = setOf("Many2one", "One2many", "Many2many")

    private fun getCoModelParameter(isKeywordArgument: Boolean) = object : FunctionParameter {
        override fun getPosition(): Int = 0
        override fun getName(): String? = if (isKeywordArgument) "comodel_name" else null
    }

    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<out PsiReference?> {
        val argumentList = psiElement.parentOfType<PyArgumentList>() ?: return emptyArray()
        val parameterExpression = argumentList.getValueExpressionForParam(getCoModelParameter(psiElement.parent is PyKeywordArgument))
                ?: return emptyArray()

        if (parameterExpression != psiElement) return emptyArray()

        val callExpression = argumentList.parent as? PyCallExpression ?: return emptyArray()
        val referenceExpression = callExpression.childrenOfType<PyReferenceExpression>()
                .firstOrNull()
                ?: return emptyArray()

        if (referenceExpression.name in classes) {
            val modelName = PyStringLiteralUtil.getStringValue(psiElement.text)
            return arrayOf(ModelNameReference(psiElement, TextRange.allOf(modelName).shiftRight(1)))
        }

        return emptyArray()
    }
}