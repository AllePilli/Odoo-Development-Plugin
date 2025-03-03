package com.github.allepilli.odoodevelopmentplugin.execution.tests

import com.github.allepilli.odoodevelopmentplugin.*
import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunType
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.github.allepilli.odoodevelopmentplugin.textcompletion.LazyTextCompletionProvider
import com.github.allepilli.odoodevelopmentplugin.textcompletion.StringValueDescriptor
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

class OdooTestConfigurationSettingsEditor(project: Project): SettingsEditor<OdooTestConfiguration>() {
    private val singleModuleNameTextCompletionProvider = LazyTextCompletionProvider(StringValueDescriptor, mutableListOf(), true) {
        ModuleDependencyIndexUtil.getAllModuleNames(project).toMutableSet()
    }

    private val commaSeparatedModuleNameTextCompletionProvider = LazyTextCompletionProvider(StringValueDescriptor, mutableListOf(','), true) {
        ModuleDependencyIndexUtil.getAllModuleNames(project).toMutableSet()
    }

    init {
        project.messageBus.connect().subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                super.exitDumbMode()

                singleModuleNameTextCompletionProvider.resetValues()
                commaSeparatedModuleNameTextCompletionProvider.resetValues()
            }
        })
    }

    private var _runTypeBox: ComboBox<OdooRunType>? = null
    private var _odooBinPathComponent: TextFieldWithBrowseButton? = null
    private var _dbNameComponent: JBTextField? = null
    private var _addonsPathsComponent: JBTextField? = null
    private var _otherOptionsTextField: ExpandableTextField? = null
    private var _modulesTextField = TextFieldWithCompletion(project, commaSeparatedModuleNameTextCompletionProvider, "", true, true, true)
    private var _testTagComponent: JBTextField? = null
    private var _testModuleComponent = TextFieldWithCompletion(project, singleModuleNameTextCompletionProvider, "", true, true, true)
    private var _testClassComponent: JBTextField? = null
    private var _testMethodComponent: JBTextField? = null
    private var _stopAfterInitComponent = JBCheckBox("Stop after Init")

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

        separator()

        row("Tag:") { _testTagComponent = textField().component }
        row("Module:") {
            cell(_testModuleComponent).resizableColumn()
                    .align(AlignX.FILL)
        }
        row("Class:") { _testClassComponent = textField().component }
        row("Method:") { _testMethodComponent = textField().component }
        row {
            cell(_stopAfterInitComponent)
        }

        separator()

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
    private var testTag by TextFieldValue(_testTagComponent)
    private var testModule by TextFieldWithCompletionValue(_testModuleComponent)
    private var testClass by TextFieldValue(_testClassComponent)
    private var testMethod by TextFieldValue(_testMethodComponent)
    private var stopAfterInit by CheckBoxValue(_stopAfterInitComponent)

    override fun resetEditorFrom(configuration: OdooTestConfiguration) {
        runType = configuration.runType
        odooBinPath = configuration.odooBinPath
        dbName = configuration.dbName
        addonsPaths = configuration.addonsPaths
        modules = configuration.odooModules
        otherOptions = configuration.otherOptions
        testTag = configuration.tag
        testModule = configuration.testModule
        testClass = configuration.testClass
        testMethod = configuration.testMethod
        stopAfterInit = configuration.stopAfterInit
    }

    override fun applyEditorTo(configuration: OdooTestConfiguration) {
        configuration.runType = runType
        configuration.odooBinPath = odooBinPath
        configuration.dbName = dbName
        configuration.addonsPaths = addonsPaths
        configuration.odooModules = modules
        configuration.otherOptions = otherOptions
        configuration.tag = testTag
        configuration.testModule = testModule
        configuration.testClass = testClass
        configuration.testMethod = testMethod
        configuration.stopAfterInit = stopAfterInit
    }

    override fun createEditor(): JComponent = myPanel
}