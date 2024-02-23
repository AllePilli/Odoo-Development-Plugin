package com.github.allepilli.odoodevelopmentplugin.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object StringsBundle {
    const val STRINGS_BUNDLE = "messages.StringsBundle"

    private val bundle = DynamicBundle(StringsBundle::class.java, STRINGS_BUNDLE)

    fun message(@PropertyKey(resourceBundle = STRINGS_BUNDLE) key: String, vararg params: Any): String =
            bundle.getMessage(key, params)

    fun messagePointer(@PropertyKey(resourceBundle = STRINGS_BUNDLE) key: String, vararg params: Any): Supplier<String> =
            bundle.getLazyMessage(key, params)
}