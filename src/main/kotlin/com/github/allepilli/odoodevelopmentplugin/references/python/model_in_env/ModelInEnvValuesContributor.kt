package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_env

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PySubscriptionExpression

private val pattern = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
        .withParent(
                PlatformPatterns.psiElement(PySubscriptionExpression::class.java)
                        .withChild(PlatformPatterns.psiElement(PyReferenceExpression::class.java))
        )

class ModelInEnvValuesContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar
            .registerReferenceProvider(pattern, ModelInEnvValuesReferenceProvider())
}