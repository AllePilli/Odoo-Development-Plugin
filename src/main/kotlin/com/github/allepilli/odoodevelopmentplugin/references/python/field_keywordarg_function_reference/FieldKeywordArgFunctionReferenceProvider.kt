package com.github.allepilli.odoodevelopmentplugin.references.python.field_keywordarg_function_reference

import com.github.allepilli.odoodevelopmentplugin.references.FunctionReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext
import org.toml.lang.psi.ext.elementType

private val functionReferenceKeywords = setOf(
        "group_expand",
        "compute",
        "inverse",
        "search",
)

private val fieldTypes = setOf(
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

class FieldKeywordArgFunctionReferenceProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (!isKeywordArgInFieldDefinition(psiElement)) return emptyArray()

        val keywordIdentifier = psiElement.siblings(forward = false, withSelf = false)
                .drop(1) // drop the '=' element
                .firstOrNull()
                ?.takeIf { it.elementType == PyTokenTypes.IDENTIFIER }
                ?: return emptyArray()

        if (keywordIdentifier.text !in functionReferenceKeywords) return emptyArray()

        val functionName = PyStringLiteralUtil.getStringValue(psiElement.text)
        return arrayOf(FunctionReference(psiElement as PyStringLiteralExpression, TextRange.allOf(functionName).shiftRight(1)))
    }

    private fun isKeywordArgInFieldDefinition(psiElement: PsiElement): Boolean {
        val argumentList = psiElement.parentOfType<PyArgumentList>() ?: return false

        val referenceExpr = argumentList.prevSibling
                ?.let { it as? PyReferenceExpression }
                ?: return false

        if (referenceExpr.lastChild.takeIf { it.elementType == PyTokenTypes.IDENTIFIER && it.text in fieldTypes } == null)
            return false

        val fieldClass = referenceExpr.reference
                .resolve()
                ?.let { it as? PyClass }
                ?: return false

        val typeEvalContext = TypeEvalContext.codeCompletion(psiElement.project, psiElement.containingFile)

        return "odoo.fields.Field" == fieldClass.getType(typeEvalContext)
                ?.getAncestorTypes(typeEvalContext)
                ?.filterIsInstance<PyClassType>()
                ?.dropLast(1) // Drop PyClassType: object
                ?.lastOrNull()
                ?.classQName
    }
}