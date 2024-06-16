package com.github.allepilli.odoodevelopmentplugin.textcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.resettableLazy
import com.intellij.util.textCompletion.TextCompletionProviderBase
import com.intellij.util.textCompletion.TextCompletionValueDescriptor

class LazyTextCompletionProvider<T>(descriptor: TextCompletionValueDescriptor<T>,
                                    separators: MutableList<Char>,
                                    caseSensitive: Boolean,
                                    valuesProducer: () -> MutableCollection<T>)
    : TextCompletionProviderBase<T>(descriptor, separators, caseSensitive) {
        private val myValues = resettableLazy(valuesProducer)

        override fun getValues(completionParameters: CompletionParameters,
                               prefix: String,
                               result: CompletionResultSet): MutableCollection<out T> = myValues.value

        fun resetValues() = myValues.reset()
    }