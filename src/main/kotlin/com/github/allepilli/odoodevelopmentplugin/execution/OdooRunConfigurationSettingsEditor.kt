package com.github.allepilli.odoodevelopmentplugin.execution

import com.github.allepilli.odoodevelopmentplugin.*
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.github.allepilli.odoodevelopmentplugin.services.OdooVersionManager
import com.github.allepilli.odoodevelopmentplugin.textcompletion.LazyTextCompletionProvider
import com.github.allepilli.odoodevelopmentplugin.textcompletion.StringValueDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.textCompletion.TextFieldWithCompletion
import javax.swing.JComponent


class OdooRunConfigurationSettingsEditor(project: Project): SettingsEditor<OdooRunConfiguration>() {
    private val moduleNameTextCompletionProvider = LazyTextCompletionProvider(StringValueDescriptor, mutableListOf(','), true) {
        ModuleDependencyIndexUtil.getAllModuleNames(project).toMutableSet()
    }

    init {
        project.messageBus.connect().subscribe(DumbService.DUMB_MODE, object: DumbService.DumbModeListener {
            override fun exitDumbMode() {
                super.exitDumbMode()

                moduleNameTextCompletionProvider.resetValues()
            }
        })
    }

    private var _runTypeBox: ComboBox<OdooRunType>? = null
    private var _odooBinPathComponent: TextFieldWithBrowseButton? = null
    private var _dbNameComponent: JBTextField? = null
    private var _addonsPathsComponent: JBTextField? = null
    private var _withDemoComponent: JBCheckBox? = null
    private var _otherOptionsTextField: ExpandableTextField? = null
    private var _modulesTextField = TextFieldWithCompletion(project, moduleNameTextCompletionProvider, "", true, true, true)
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
        row("Additional Addon Paths") {
            rowComment("Addon paths additional to the ones chosen in the 'General Odoo Settings', these should be relative to the project root directory.")
            _addonsPathsComponent = textField().component
        }
        row("Modules:") {
            cell(_modulesTextField).resizableColumn()
                    .align(AlignX.FILL)
                    .comment("Comma separated list of module names")
        }

        if (project.service<OdooVersionManager>().getVersion() == VersionConstants.SETTING_WITH_DEMO) {
            row {
                _withDemoComponent = checkBox("With demo").component
            }
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
    private var withDemo by CheckBoxValue(_withDemoComponent)
    private var otherOptions by TextFieldValue(_otherOptionsTextField)

    override fun resetEditorFrom(configuration: OdooRunConfiguration) {
        runType = configuration.runType
        odooBinPath = configuration.odooBinPath
        dbName = configuration.dbName
        addonsPaths = configuration.addonsPaths
        modules = configuration.odooModules

        if (configuration.project.service<OdooVersionManager>().getVersion() == VersionConstants.SETTING_WITH_DEMO) {
            withDemo = configuration.withDemo
        }

        otherOptions = configuration.otherOptions
    }

    override fun applyEditorTo(configuration: OdooRunConfiguration) {
        configuration.runType = runType
        configuration.odooBinPath = odooBinPath
        configuration.dbName = dbName
        configuration.addonsPaths = addonsPaths
        configuration.odooModules = modules

        if (configuration.project.service<OdooVersionManager>().getVersion() == VersionConstants.SETTING_WITH_DEMO) {
            configuration.withDemo = withDemo
        }

        configuration.otherOptions = otherOptions
    }

    override fun createEditor(): JComponent = myPanel
}