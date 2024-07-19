package com.github.allepilli.odoodevelopmentplugin.execution

import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.TargetEnvironment
import com.intellij.execution.target.value.TraceableTargetEnvironmentFunction
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import com.intellij.terminal.TerminalExecutionConsole
import com.intellij.util.io.BaseOutputReader
import com.jetbrains.python.run.*
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest
import java.io.File
import java.nio.file.Path
import java.util.function.Function

@Suppress("UnstableApiUsage")
class OdooRunCommandLineState(private val runConfiguration: OdooRunConfiguration, env: ExecutionEnvironment?) : PythonCommandLineState(runConfiguration, env) {
    companion object {
        private const val TERM_COLOR_STRING = "xterm-256color"
        private val termColorFunction = object : TraceableTargetEnvironmentFunction<String>() {
            override fun applyInner(t: TargetEnvironment): String = TERM_COLOR_STRING
        }

        private fun createTerminalExecutionConsole(project: Project, processHandler: ProcessHandler): TerminalExecutionConsole =
                TerminalExecutionConsole(project, processHandler).apply {
                    addMessageFilter(PythonTracebackFilter(project))
                    addMessageFilter(UrlFilter())
                }
    }

    override fun buildPythonExecution(helpersAwareRequest: HelpersAwareTargetEnvironmentRequest): PythonExecution =
            PythonScriptExecution().apply {
                val odooBin = File(runConfiguration.odooBinPath)
                pythonScriptPath = getTargetPath(helpersAwareRequest.targetEnvironmentRequest, Path.of(odooBin.absolutePath))
                workingDir = getPythonExecutionWorkingDir(helpersAwareRequest.targetEnvironmentRequest)

                val addonPaths = GeneralSettingsState.getInstance(runConfiguration.project)
                        .addonPaths
                        .filterNot { it.endsWith("odoo/odoo/addons") } // odoo core modules should not be added to the run config
                        .toMutableList()

                runConfiguration.addonsPaths
                        .split(',')
                        .filter { runConfigAddonPath -> addonPaths.none { it.endsWith(runConfigAddonPath) } }
                        .forEach {
                            addonPaths.add("${runConfiguration.project.basePath}/$it")
                        }

                addParameter("--addons-path=${addonPaths.joinToString(separator = ",")}")
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

    private fun executeWithCMDEmulation(processHandler: ProcessHandler): ExecutionResult {
        val executeConsole = createTerminalExecutionConsole(runConfiguration.project, processHandler)
        processHandler.startNotify()

        return DefaultExecutionResult(executeConsole, processHandler, *createActions(executeConsole, processHandler))
    }

    override fun execute(executor: Executor?, processStarter: PythonProcessStarter?, vararg patchers: CommandLinePatcher?): ExecutionResult {
        setRunWithPty(true)

        val processHandler = startProcess(processStarter, *patchers)
        return executeWithCMDEmulation(processHandler)
    }

    override fun execute(executor: Executor, converter: PythonScriptTargetedCommandLineBuilder): ExecutionResult? {
        setRunWithPty(true)

        val processHandler = startProcess(converter)
        return executeWithCMDEmulation(processHandler)
    }

    override fun createActions(console: ConsoleView?, processHandler: ProcessHandler?, executor: Executor?): Array<out AnAction?> {
        val superActions = super.createActions(console, processHandler, executor)
        val openDBAction = ActionManager.getInstance().getAction("OpenDbAction") ?: return superActions
        return superActions + openDBAction
    }

    override fun customizeEnvironmentVars(envs: MutableMap<String, String>?, passParentEnvs: Boolean) {
        super.customizeEnvironmentVars(envs, passParentEnvs)
        if (!SystemInfo.isWindows) envs?.put("TERM", TERM_COLOR_STRING)
    }

    override fun customizePythonExecutionEnvironmentVars(helpersAwareTargetRequest: HelpersAwareTargetEnvironmentRequest, envs: MutableMap<String, Function<TargetEnvironment, String>>, passParentEnvs: Boolean) {
        super.customizePythonExecutionEnvironmentVars(helpersAwareTargetRequest, envs, passParentEnvs)
        if (!SystemInfo.isWindows) envs["TERM"] = termColorFunction
    }

    override fun doCreateProcess(commandLine: GeneralCommandLine): ProcessHandler =
            object: OSProcessHandler(commandLine) {
                override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forTerminalPtyProcess()
            }
}