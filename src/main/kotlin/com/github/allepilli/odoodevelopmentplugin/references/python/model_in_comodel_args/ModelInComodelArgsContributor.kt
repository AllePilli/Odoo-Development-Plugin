package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_comodel_args

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStringLiteralExpression

private val pattern = PlatformPatterns.or(
        PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
                .withParents(PyKeywordArgument::class.java, PyArgumentList::class.java, PyCallExpression::class.java),
        PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
                .withParents(PyArgumentList::class.java, PyCallExpression::class.java),
)


class ModelInComodelArgsContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar
            .registerReferenceProvider(pattern, ModelInComodelArgsReferenceProvider())
}