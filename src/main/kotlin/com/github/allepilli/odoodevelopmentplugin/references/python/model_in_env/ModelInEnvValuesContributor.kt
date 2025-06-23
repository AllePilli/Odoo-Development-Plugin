package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_env

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PySubscriptionExpression

class ModelInEnvContributor: OdooReferenceContributor(::ModelInEnvValuesReferenceProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = psiElement<PyStringLiteralExpression> {
            parent<PySubscriptionExpression> {
                // env
                child<PyReferenceExpression> {
                    // self
                    child<PyReferenceExpression>()
                }
            }
        }
}