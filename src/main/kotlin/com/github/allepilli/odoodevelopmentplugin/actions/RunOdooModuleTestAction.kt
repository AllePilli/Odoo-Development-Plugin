package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfiguration
import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfigurationFactory
import com.github.allepilli.odoodevelopmentplugin.extensions.isOdooModuleDirectory
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class RunOdooModuleTestAction: AbstractRunOdooTestAction() {
    override fun createSettings(e: AnActionEvent): RunnerAndConfigurationSettings? {
        val runManager = RunManager.getInstance(e.project ?: return null)
        val moduleName = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name ?: return null
        val runConfSettings =
                runManager.createConfiguration(moduleName, OdooTestConfigurationFactory())

        (runConfSettings.configuration as? OdooTestConfiguration ?: return null).apply {
            odooModules = moduleName
            testModule = moduleName
        }

        return runConfSettings
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        e.presentation.isEnabledAndVisible = file != null && file.isOdooModuleDirectory(project) && file.findChild("tests") != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}