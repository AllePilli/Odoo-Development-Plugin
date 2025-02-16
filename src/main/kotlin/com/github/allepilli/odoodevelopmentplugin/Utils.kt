package com.github.allepilli.odoodevelopmentplugin

import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBTextField
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyUtil
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.reflect.KProperty

private val simpleCommandLineDelimiterRgx = """\s+""".toRegex()
private val utf8 = Charset.forName("UTF-8")

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

fun simpleCommandLine(command: String, project: Project): GeneralCommandLine =
        simpleCommandLine(command, project.guessProjectDir()?.path?.let { Path.of(it) })

fun simpleCommandLine(command: String, workingDirectory: Path? = null): GeneralCommandLine =
        GeneralCommandLine(command.split(simpleCommandLineDelimiterRgx))
                .withCharset(utf8)
                .withWorkingDirectory(workingDirectory)

fun getModuleVirtualFile(project: Project, moduleName: String): VirtualFile? =
        GeneralSettingsState.getInstance(project).addonPaths
                .asSequence()
                .mapNotNull {
                    val modulePath = "$it/$moduleName".toNioPathOrNull()!!
                    VirtualFileManager.getInstance().findFileByNioPath(modulePath)
                }
                .firstOrNull()