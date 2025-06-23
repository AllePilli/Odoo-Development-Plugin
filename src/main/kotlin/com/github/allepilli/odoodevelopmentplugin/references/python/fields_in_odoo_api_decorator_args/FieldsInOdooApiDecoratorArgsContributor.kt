package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_odoo_api_decorator_args

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.string
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*

private val decoratorNames = setOf(
        "api.constrains",
        "api.onchange",
        "api.depends",
)

class FieldsInOdooApiDecoratorArgsContributor: OdooReferenceContributor(::FieldsInOdooApiDecoratorArgsProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = psiElement<PyStringLiteralExpression> {
            parent<PyArgumentList> {
                parent<PyCallExpression> {
                    child<PyReferenceExpression> {
                        text(string { oneOf(decoratorNames) })
                    }
                    parent<PyDecorator>()
                }
            }
        }
}