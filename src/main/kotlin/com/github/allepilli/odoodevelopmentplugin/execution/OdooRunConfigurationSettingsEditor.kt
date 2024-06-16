package com.github.allepilli.odoodevelopmentplugin.execution

import com.github.allepilli.odoodevelopmentplugin.ComboBoxValue
import com.github.allepilli.odoodevelopmentplugin.TextFieldValue
import com.github.allepilli.odoodevelopmentplugin.TextFieldWithBrowseButtonValue
import com.github.allepilli.odoodevelopmentplugin.TextFieldWithCompletionValue
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.github.allepilli.odoodevelopmentplugin.textcompletion.LazyTextCompletionProvider
import com.github.allepilli.odoodevelopmentplugin.textcompletion.StringValueDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.textCompletion.TextFieldWithCompletion
import javax.swing.JComponent


class OdooRunConfigurationSettingsEditor(project: Project): SettingsEditor<OdooRunConfiguration>() {
    private val textCompletionProvider = LazyTextCompletionProvider(StringValueDescriptor, mutableListOf(','), true) {
        ModuleDependencyIndexUtil.getAllModuleNames(project).toMutableSet()
    }

    init {
        project.messageBus.connect().subscribe(DumbService.DUMB_MODE, object: DumbService.DumbModeListener {
            override fun exitDumbMode() {
                super.exitDumbMode()

                textCompletionProvider.resetValues()
            }
        })
    }

    private var _runTypeBox: ComboBox<OdooRunType>? = null
    private var _odooBinPathComponent: TextFieldWithBrowseButton? = null
    private var _dbNameComponent: JBTextField? = null
    private var _addonsPathsComponent: JBTextField? = null
    private var _otherOptionsTextField: ExpandableTextField? = null
    private var _modulesTextField = TextFieldWithCompletion(project, textCompletionProvider, "", true, true, true)
    private val myPanel = panel {
        row("Run Type:") {
            _runTypeBox = comboBox(items = OdooRunType.entries, textListCellRenderer { it?.presentableName })
                    .component
        }
        row("odoo-bin path:") {
            _odooBinPathComponent = textFieldWithBrowseButton("Select odoo-bin File")
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .component
        }
        row("Database Name:") { _dbNameComponent = textField().component }
        row("Addons Path(s)") {
            _addonsPathsComponent = textField()
                    .comment("Paths relative to the project root directory")
                    .component
        }
        row("Modules:") {
            cell(_modulesTextField).resizableColumn()
                    .align(AlignX.FILL)
                    .comment("Comma separated list of module names")
        }
        row("Other Options:") {
            _otherOptionsTextField = expandableTextField()
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .component
        }
    }

    private var runType by ComboBoxValue(_runTypeBox, OdooRunType.NONE)
    private var odooBinPath by TextFieldWithBrowseButtonValue(_odooBinPathComponent)
    private var dbName by TextFieldValue(_dbNameComponent)
    private var addonsPaths by TextFieldValue(_addonsPathsComponent)
    private var modules by TextFieldWithCompletionValue(_modulesTextField)
    private var otherOptions by TextFieldValue(_otherOptionsTextField)

    override fun resetEditorFrom(configuration: OdooRunConfiguration) {
        runType = configuration.runType
        odooBinPath = configuration.odooBinPath
        dbName = configuration.dbName
        addonsPaths = configuration.addonsPaths
        modules = configuration.odooModules
        otherOptions = configuration.otherOptions
    }

    override fun applyEditorTo(configuration: OdooRunConfiguration) {
        configuration.runType = runType
        configuration.odooBinPath = odooBinPath
        configuration.dbName = dbName
        configuration.addonsPaths = addonsPaths
        configuration.odooModules = modules
        configuration.otherOptions = otherOptions
    }

    override fun createEditor(): JComponent = myPanel
}