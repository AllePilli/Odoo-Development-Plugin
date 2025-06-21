package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_odoo_api_decorator_args

import com.github.allepilli.odoodevelopmentplugin.references.SimpleFieldNameReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil

class FieldsInOdooApiDecoratorArgsProvider: PyStringLiteralReferenceProvider() {
    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> {
        val fieldName = PyStringLiteralUtil.getStringValue(element.text)

        val dotIndices = fieldName.mapIndexed { idx, c -> if (c == '.') idx else null }
                .filterNotNull()
                .toMutableList()
                .apply {
                    add(fieldName.length) // append a "virtual" dot at the end of the fieldName
                }

        var prevDotIdx = -1

        return buildList<SimpleFieldNameReference> {
            for (dotIdx in dotIndices) {
                val textRange = TextRange.create(prevDotIdx + 1, dotIdx).shiftRight(1)
                val modelName = lastOrNull()
                        ?.apply { multiResolve(false) } // Do the calculations
                        ?.coModelName

                if (modelName == null && prevDotIdx > 0)
                    // prevDotIdx > 0 means we are not checking the first field in the chain
                    // we encountered field access on a non-relational field
                    // which should not be possible, but people make mistakes ¯\_(ツ)_/¯
                    break

                add(SimpleFieldNameReference(element, textRange, modelName))
                prevDotIdx = dotIdx
            }
        }
    }
}