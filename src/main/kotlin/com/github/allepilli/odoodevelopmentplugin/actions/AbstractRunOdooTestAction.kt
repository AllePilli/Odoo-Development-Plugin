package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunType
import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfiguration
import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

abstract class AbstractRunOdooTestAction : AnAction("Run Odoo Test", "Run odoo test", OdooIcons.odoo) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val runManager = RunManager.getInstance(project)
        val settings = createSettings(e) ?: return

        try {
            (settings.configuration as OdooTestConfiguration).addCommonTestData(project)
        } catch (e: DialogFailedException) {
            return
        }

        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    abstract fun createSettings(e: AnActionEvent): RunnerAndConfigurationSettings?

    private fun OdooTestConfiguration.addCommonTestData(project: Project) {
        val generalSettings = GeneralSettingsState.getInstance(project)

        if (generalSettings.defaultTestDbName.trim().isEmpty() || generalSettings.defaultOdooBinPath.trim().isEmpty()) {
            // Create dialog that requests the appropriate data
            var focusComponent: JComponent? = null
            val components = mutableMapOf<String, JComponent>()
            val success = DialogBuilder(project)
                    .title("Add Missing Run Data")
                    .centerPanel(panel {
                        row {
                            comment("Default value(s) can be set in the Odoo General Settings")
                        }

                        if (generalSettings.defaultTestDbName.trim().isEmpty()) {
                            row("Db name:") {
                                components["dbName"] = textField()
                                        .validationOnApply {
                                            if (it.text.trim().isEmpty()) error("Db name is required")
                                            else null
                                        }
                                        .onApply {
                                            dbName = (components["dbName"] as JBTextField).text
                                        }
                                        .component
                                focusComponent = components["dbName"]
                            }
                        }

                        if (generalSettings.defaultOdooBinPath.trim().isEmpty()) {
                            row("odoo-bin path:") {
                                components["odooBinPath"] = textFieldWithBrowseButton("Select odoo-bin File")
                                        .resizableColumn()
                                        .align(AlignX.FILL)
                                        .validationOnApply {
                                            if (it.text.trim().isEmpty()) error("Odoo-bin path is required")
                                            else null
                                        }
                                        .onApply {
                                            odooBinPath = (components["odooBinPath"] as TextFieldWithBrowseButton).text
                                        }
                                        .component

                                if (focusComponent == null) focusComponent = components["odooBinPath"]
                            }
                        }
                    })
                    .apply {
                        addOkAction()
                        addCancelAction()
                    }
                    .showAndGet()

            if (success) {
                runType = OdooRunType.INIT
                stopAfterInit = true
                dbName = (components["dbName"] as? JBTextField?)?.text ?: generalSettings.defaultTestDbName
                odooBinPath = (components["odooBinPath"] as? TextFieldWithBrowseButton?)?.text ?: generalSettings.defaultOdooBinPath
            } else throw DialogFailedException()
        } else {
            runType = OdooRunType.INIT
            dbName = generalSettings.defaultTestDbName
            stopAfterInit = true
            odooBinPath = generalSettings.defaultOdooBinPath
        }
    }

    private class DialogFailedException: Exception()
}