package com.github.allepilli.odoodevelopmentplugin.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

abstract class BasicCompletionContributor<T: PsiElement>(pattern: ElementPattern<T>): CompletionContributor() {
    abstract fun getCompletions(parameters: CompletionParameters, context: ProcessingContext): List<LookupElement>

    init {
        extend(CompletionType.BASIC, pattern, object: CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
                resultSet.addAllElements(getCompletions(parameters, context))
            }
        })
    }
}