package com.github.allepilli.odoodevelopmentplugin.references.python.model_in_comodel_args

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStringLiteralExpression

class ModelInComodelArgsContributor: OdooReferenceContributor(::ModelInComodelArgsReferenceProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = PlatformPatterns.or(
                psiElement<PyStringLiteralExpression> {
                    parents(PyKeywordArgument::class, PyArgumentList::class, PyCallExpression::class)
                },
                psiElement<PyStringLiteralExpression> {
                    parents(PyArgumentList::class, PyCallExpression::class)
                }
        )
}