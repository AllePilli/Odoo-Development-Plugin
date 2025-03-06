package com.github.allepilli.odoodevelopmentplugin.settings.general

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class GeneralSettingsComponent(project: Project) {
    private val pathsList = PathsList(project)
    private var odooBinPathComponent: TextFieldWithBrowseButton? = null
    private var defaultTestDbNameComponent: JBTextField? = null

    val panel = FormBuilder.createFormBuilder()
            .addComponent(
                    panel {
                        group("Addon Paths", indent = false) {
                            row {
                                comment("These paths will be used to discover modules and their models.")
                            }
                        }
                    }
            )
            .addComponent(pathsList.component)
            .addComponent(
                    panel {
                        separator()
                        row("Default odoo-bin path:") {
                            odooBinPathComponent = textFieldWithBrowseButton("Select odoo-bin File")
                                    .resizableColumn()
                                    .align(AlignX.FILL)
                                    .component
                        }
                        row("Default test-database name:") {
                            defaultTestDbNameComponent = textField().component
                        }
                    }
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel!!

    var addonPaths: List<String>
        get() = pathsList.items
        set(value) { pathsList.items = value }

    var defaultOdooBinPath: String
        get() = odooBinPathComponent?.text ?: ""
        set(value) { odooBinPathComponent?.text = value }

    var defaultTestDbName: String
        get() = defaultTestDbNameComponent?.text ?: ""
        set(value) { defaultTestDbNameComponent?.text = value }

    private class PathsList(private val project: Project) {
        companion object {
            private val directoryChooserDescriptor = FileChooserDescriptor(
                    false,
                    true,
                    false,
                    false,
                    false,
                    false,
            )
        }

        private val list = JBList<String>(MutableCollectionComboBoxModel()).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = textListCellRenderer { it }
        }

        private val toolbarDecorator = ToolbarDecorator.createDecorator(list)
                .setAddAction {
                    FileChooser.chooseFile(directoryChooserDescriptor, project, null) { selectedFile ->
                        (list.model as MutableCollectionComboBoxModel).addItem(selectedFile.path)
                    }
                }

        val component: JPanel
            get() = toolbarDecorator.createPanel()

        var items: List<String>
            get() = (list.model as MutableCollectionComboBoxModel).items
            set(value) { (list.model as MutableCollectionComboBoxModel).replaceAll(value) }
    }
}