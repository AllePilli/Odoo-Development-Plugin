package com.github.allepilli.odoodevelopmentplugin.dialogs

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenItemSelectedFromUi
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import javax.swing.JComponent
import javax.swing.ListCellRenderer

class ManifestFormDialog(project: Project, title: String = StringsBundle.message("dialog.ManifestFormDialog.title")): DialogWrapper(project) {
    var name = ""
    var version = ""
    var category = ""
    var description = ""

    private var _license: License? = License.NONE
    private var customLicense: String = ""
    val license: String
        get() = (_license ?: License.NONE)
                .takeUnless { it == License.OTHER }
                ?.value
                ?: customLicense

    init {
        this.title = title
        init()
    }

    enum class License(val value: String) {
        LGPL3("LGPL-3"),
        OEEL1("OEEL-1"),
        OTHER("Other"),
        NONE("");

        companion object {
            val renderer: ListCellRenderer<License?> = textListCellRenderer { it?.value }
        }
    }

    override fun createCenterPanel(): JComponent = panel {
        row(StringsBundle.message("dialog.ManifestFormDialog.label.module.name")) {
            textField().bindText(this@ManifestFormDialog::name)
        }
        row(StringsBundle.message("dialog.ManifestFormDialog.label.version")) {
            textField().bindText(this@ManifestFormDialog::version)
                    .applyToComponent { text = "1.0" }
        }
        row(StringsBundle.message("dialog.ManifestFormDialog.label.category")) {
            textField().bindText(this@ManifestFormDialog::category)
        }
        row(StringsBundle.message("dialog.ManifestFormDialog.label.description")) {
            textArea().bindText(this@ManifestFormDialog::description)
                    .applyToComponent { rows = 4 }
        }
        row(StringsBundle.message("dialog.ManifestFormDialog.label.license")) {
            val licenseBox = comboBox(items = License.entries, renderer = License.renderer)
                    .bindItem(this@ManifestFormDialog::_license)

            textField().bindText(this@ManifestFormDialog::customLicense)
                    .visibleIf(object : ObservableProperty<Boolean> {
                        override fun get(): Boolean = _license == License.OTHER
                        override fun afterChange(parentDisposable: Disposable?, listener: (Boolean) -> Unit) {
                            licenseBox.whenItemSelectedFromUi {
                                listener(it == License.OTHER)
                            }
                        }
                    })
        }
    }
}