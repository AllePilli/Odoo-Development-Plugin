package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_env

import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.childrenOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralUtil
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext

class ModelInEnvValuesReferenceProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val referenceExpression = psiElement.parent.childrenOfType<PyReferenceExpression>().singleOrNull() ?: return emptyArray()
        val typeEvalContext = TypeEvalContext.codeAnalysis(psiElement.project, psiElement.containingFile)
        val resolveElement = referenceExpression.followAssignmentsChain(PyResolveContext.implicitContext(typeEvalContext)).element

        if (resolveElement is PyNamedParameter) {
            val type = typeEvalContext.getType(resolveElement) ?: return emptyArray()

            if (type.declarationElement?.qualifiedName == "odoo.api.Environment") {
                val modelName = PyStringLiteralUtil.getStringValue(psiElement.text)
                return arrayOf(ModelNameReference(psiElement, TextRange.allOf(modelName).shiftRight(1)))
            }
        }

        return emptyArray()
    }
}