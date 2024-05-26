package com.github.allepilli.odoodevelopmentplugin.references.python.compute_function

import com.github.allepilli.odoodevelopmentplugin.references.ComputeFunctionReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.FunctionParameter
import com.jetbrains.python.psi.*

class ComputeFunctionReferenceProvider: PsiReferenceProvider() {
    private val classes = setOf(
            "Boolean",
            "Integer",
            "Float",
            "Monetary",
            "Char",
            "Text",
            "Html",
            "Date",
            "Datetime",
            "Binary",
            "Image",
            "Selection",
            "Reference",
            "Many2one",
            "Many2oneReference",
            "Json",
            "Properties",
            "PropertiesDefinition",
            "One2many",
            "Many2many",
            "Id",
    )

    private val computeParameter = object : FunctionParameter {
        override fun getPosition(): Int = FunctionParameter.POSITION_NOT_SUPPORTED
        override fun getName(): String = "compute"
    }

    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val argumentList = psiElement.parentOfType<PyArgumentList>() ?: return emptyArray()
        val parameterExpression = argumentList.getValueExpressionForParam(computeParameter)
                ?: return emptyArray()

        if (parameterExpression != psiElement) return emptyArray()

        val callExpression = argumentList.parent as? PyCallExpression ?: return emptyArray()
        val referenceExpression = callExpression.childrenOfType<PyReferenceExpression>()
                .firstOrNull()
                ?: return emptyArray()

        if (referenceExpression.name in classes) {
            val functionName = PyStringLiteralUtil.getStringValue(psiElement.text)
            return arrayOf(ComputeFunctionReference(psiElement as PyStringLiteralExpression, TextRange.allOf(functionName).shiftRight(1)))
        }

        return emptyArray()
    }
}