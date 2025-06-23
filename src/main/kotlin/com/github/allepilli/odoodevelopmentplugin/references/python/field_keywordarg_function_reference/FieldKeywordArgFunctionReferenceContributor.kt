package com.github.allepilli.odoodevelopmentplugin.references.python.field_keywordarg_function_reference

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStringLiteralExpression

class FieldKeywordArgFunctionReferenceContributor: OdooReferenceContributor(::FieldKeywordArgFunctionReferenceProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = psiElement<PyStringLiteralExpression> {
            parents(PyKeywordArgument::class, PyArgumentList::class, PyCallExpression::class)
        }
}