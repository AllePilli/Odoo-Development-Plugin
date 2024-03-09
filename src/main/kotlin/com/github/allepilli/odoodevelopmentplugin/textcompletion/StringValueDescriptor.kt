package com.github.allepilli.odoodevelopmentplugin.textcompletion

import com.intellij.util.textCompletion.DefaultTextCompletionValueDescriptor

object StringValueDescriptor: DefaultTextCompletionValueDescriptor<String>() {
    override fun getLookupString(string: String): String = string
}