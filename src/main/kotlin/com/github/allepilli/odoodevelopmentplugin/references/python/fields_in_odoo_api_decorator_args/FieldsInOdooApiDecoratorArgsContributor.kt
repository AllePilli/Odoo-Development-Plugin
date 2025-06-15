package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_odoo_api_decorator_args

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.string
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.*

private val decoratorNames = setOf(
        "api.constrains",
        "api.onchange",
        "api.depends",
)

private val pattern = psiElement<PyStringLiteralExpression> {
    parent<PyArgumentList> {
        parent<PyCallExpression> {
            child<PyReferenceExpression> {
                text(string { oneOf(decoratorNames) })
            }
            parent<PyDecorator>()
        }
    }
}

class FieldsInOdooApiDecoratorArgsContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar
            .registerReferenceProvider(pattern, FieldsInOdooApiDecoratorArgsProvider())
}