package com.github.allepilli.odoodevelopmentplugin.execution

import com.intellij.execution.Executor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import com.jetbrains.python.run.PythonCommandLineState
import com.jetbrains.python.run.PythonExecution
import com.jetbrains.python.run.PythonScriptExecution
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest
import java.io.File
import java.nio.file.Path

@Suppress("UnstableApiUsage")
class OdooRunCommandLineState(private val runConfiguration: OdooRunConfiguration, env: ExecutionEnvironment?) : PythonCommandLineState(runConfiguration, env) {
    override fun buildPythonExecution(helpersAwareRequest: HelpersAwareTargetEnvironmentRequest): PythonExecution =
            PythonScriptExecution().apply {
                val odooBin = File(runConfiguration.odooBinPath)
                pythonScriptPath = getTargetPath(helpersAwareRequest.targetEnvironmentRequest, Path.of(odooBin.absolutePath))
                workingDir = getPythonExecutionWorkingDir(helpersAwareRequest.targetEnvironmentRequest)

                addParameter("--addons-path=${runConfiguration.addonsPaths}")
                addParameters("--database", runConfiguration.dbName)

                when (runConfiguration.runType) {
                    OdooRunType.INIT -> addParameter("-i")
                    OdooRunType.UPDATE -> addParameter("-u")
                    OdooRunType.NONE -> throw IllegalStateException("Odoo run type should not be of type NONE")
                }
                addParameter(runConfiguration.odooModules)

                val otherOptions = runConfiguration.otherOptions.trim()
                if (otherOptions.isNotEmpty()) addParameters(otherOptions.split(' '))

                charset = EncodingProjectManager.getInstance(runConfiguration.project).defaultCharset
            }

    override fun createActions(console: ConsoleView?, processHandler: ProcessHandler?, executor: Executor?): Array<out AnAction?> {
        val superActions = super.createActions(console, processHandler, executor)
        val openDBAction = ActionManager.getInstance().getAction("OpenDbAction") ?: return superActions
        return superActions + openDBAction
    }
}