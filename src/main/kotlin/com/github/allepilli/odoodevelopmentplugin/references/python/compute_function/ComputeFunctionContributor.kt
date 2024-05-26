package com.github.allepilli.odoodevelopmentplugin.references.python.compute_function

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStringLiteralExpression

private val pattern = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
        .withParents(PyKeywordArgument::class.java, PyArgumentList::class.java, PyCallExpression::class.java)

class ComputeFunctionContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) =
            registrar.registerReferenceProvider(pattern, ComputeFunctionReferenceProvider())
}