package com.github.allepilli.odoodevelopmentplugin.references.python.field_keywordarg_function_reference

import com.github.allepilli.odoodevelopmentplugin.references.FunctionReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
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

private val IS_FIELD_ARG_LIST_KEY = Key.create<Boolean>("odoo_development_plugin_is_field_arg_list_key")

class FieldKeywordArgFunctionReferenceProvider: PyStringLiteralReferenceProvider() {
    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> {
        if (!isKeywordArgInFieldDefinition(element)) return emptyList()

        val keywordIdentifier = element.siblings(forward = false, withSelf = false)
                .drop(1) // drop the '=' element
                .firstOrNull()
                ?.takeIf { it.elementType == PyTokenTypes.IDENTIFIER }
                ?: return emptyList()

        if (keywordIdentifier.text !in functionReferenceKeywords) return emptyList()

        return listOf(FunctionReference(element, PyStringLiteralUtil.getContentRange(element.text)))
    }

    private fun isKeywordArgInFieldDefinition(psiElement: PsiElement): Boolean {
        val argumentList = psiElement.parentOfType<PyArgumentList>() ?: return false
        val isFieldArgList = argumentList.getUserData(IS_FIELD_ARG_LIST_KEY)
        if (isFieldArgList != null) return isFieldArgList

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

        val isKeywordArgInField = "odoo.fields.Field" == fieldClass.getType(typeEvalContext)
                ?.getAncestorTypes(typeEvalContext)
                ?.filterIsInstance<PyClassType>()
                ?.dropLast(1) // Drop PyClassType: object
                ?.lastOrNull()
                ?.classQName

        argumentList.putUserData(IS_FIELD_ARG_LIST_KEY, isKeywordArgInField)

        return isKeywordArgInField
    }
}