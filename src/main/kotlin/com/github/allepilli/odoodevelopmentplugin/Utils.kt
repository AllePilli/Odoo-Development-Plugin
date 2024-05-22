package com.github.allepilli.odoodevelopmentplugin

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBTextField
import com.intellij.util.textCompletion.TextFieldWithCompletion
import kotlin.reflect.KProperty

inline fun <reified E> buildArray(builderAction: MutableList<E>.() -> Unit): Array<E> = buildList(builderAction).toTypedArray()

fun findModule(name: String, project: Project): VirtualFile? {
    val virtualFileManager = VirtualFileManager.getInstance()
    val basePath = project.basePath ?: return null

    return virtualFileManager.findFileByUrl("file://$basePath/odoo/addons/$name")
            ?: virtualFileManager.findFileByUrl("file://$basePath/enterprise/$name")
            ?: virtualFileManager.findFileByUrl("file://$basePath/odoo/odoo/addons/$name")
}

fun getContainingModule(psiFile: PsiFile): VirtualFile? {
    val moduleName = psiFile.getContainingModuleName()
    if (moduleName == null) {
        Logger.getInstance("PSI").warn("Could not find module name for $psiFile")
        return null
    }

    val module = findModule(moduleName, psiFile.project)
    if (module == null) Logger.getInstance("PSI").warn("Could not find module for $psiFile")

    return module
}

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