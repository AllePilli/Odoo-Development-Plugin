package com.github.allepilli.odoodevelopmentplugin

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.ui.components.JBTextField
import com.intellij.util.textCompletion.TextFieldWithCompletion
import kotlin.reflect.KProperty

inline fun <reified E> buildArray(builderAction: MutableList<E>.() -> Unit): Array<E> = buildList(builderAction).toTypedArray()

class TextFieldValue(private val textField: JBTextField?, private val trimValue: Boolean = true) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            (if (trimValue) textField?.text?.trim() else textField?.text) ?: ""
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        textField?.text = if (trimValue) value.trim() else value
    }
}

class TextFieldWithCompletionValue(private val textField: TextFieldWithCompletion, private val trimValue: Boolean = true) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            if (trimValue) textField.text.trim() else textField.text
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        textField.text = if (trimValue) value.trim() else value
    }
}

class TextFieldWithBrowseButtonValue(private val textField: TextFieldWithBrowseButton?, private val trimValue: Boolean = true) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            (if (trimValue) textField?.text?.trim() else textField?.text) ?: ""
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        textField?.text = if (trimValue) value.trim() else value
    }
}

class ComboBoxValue<E>(private val comboBox: ComboBox<E>?, private val defaultValue: E) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): E = comboBox?.item ?: defaultValue
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: E) { comboBox?.item = value }
}

fun <T> computeReadAction(computable: () -> T): T = ReadAction.compute<T, RuntimeException>(ThrowableComputable(computable))