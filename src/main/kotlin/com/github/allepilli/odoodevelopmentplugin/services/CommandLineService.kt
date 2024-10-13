package com.github.allepilli.odoodevelopmentplugin.services

import com.github.allepilli.odoodevelopmentplugin.simpleCommandLine
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path

@Service(Service.Level.APP)
class CommandLineService(
        private val cs: CoroutineScope,
) {
    fun runCommand(generalCommandLine: GeneralCommandLine, callback: (output: String) -> Unit = {}) {
        cs.launch {
            val output = ScriptRunnerUtil.getProcessOutput(generalCommandLine,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    30000)
            callback(output)
        }
    }

    fun runCommand(command: String, workingDirectory: Path? = null, callback: (output: String) -> Unit = {}) {
        val commandLine = simpleCommandLine(command, workingDirectory)
        runCommand(commandLine, callback)
    }

    fun runCommand(command: String, project: Project, callback: (output: String) -> Unit = {}) {
        val commandLine = simpleCommandLine(command, project)
        runCommand(commandLine, callback)
    }
}
