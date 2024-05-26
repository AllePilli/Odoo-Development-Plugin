package com.github.allepilli.odoodevelopmentplugin.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil
import com.jetbrains.python.psi.types.TypeEvalContext

class ComputeFunctionReference(element: PyStringLiteralExpression, rangeInElement: TextRange?) : PsiReferenceBase<PyStringLiteralExpression>(element, rangeInElement) {
    override fun resolve(): PsiElement? {
        val pyClass = element.parentOfType<PyClass>() ?: return null
        val functionName = PyStringLiteralUtil.getStringValue(element.text)

        return pyClass.findMethodByName(functionName, false, TypeEvalContext.codeAnalysis(element.project, element.containingFile))
    }
}