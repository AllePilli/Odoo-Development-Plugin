package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunConfiguration
import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunType
import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfiguration
import com.github.allepilli.odoodevelopmentplugin.notifications.OdooBalloonNotifier
import com.github.allepilli.odoodevelopmentplugin.services.CommandLineService
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.ide.ActivityTracker
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import java.util.concurrent.atomic.AtomicBoolean

class DropDBAction: AnAction() {
    companion object {
        // size in characters
        private const val MAX_DB_NAME_SIZE = 15
        private const val DB_NAME_SUFFIX_LENGTH = 7

        private val DB_IS_ACCESSED_RGX = """.*?ERROR: (.*?)\nDETAIL: (.*?)$""".toRegex()

        private fun calcText(dbName: String): String =
                if (dbName.isNotEmpty() && dbName.isNotBlank()) {
                    "Drop " + StringUtil.shortenTextWithEllipsis(dbName,
                            MAX_DB_NAME_SIZE,
                            DB_NAME_SUFFIX_LENGTH,
                            true)
                }
                else "Drop DB"
    }

    private var currentDBName: String? = null
    private var isRunning = AtomicBoolean(false)
    private var selectedRunConfiguration: RunConfiguration? = null
    private var selectedDbName: String?
        get() = when (selectedRunConfiguration) {
            is OdooRunConfiguration -> (selectedRunConfiguration as? OdooRunConfiguration)?.dbName
            is OdooTestConfiguration -> (selectedRunConfiguration as? OdooTestConfiguration)?.dbName
            else -> null
        }
        set(value) {
            if (value == null) return
            when (selectedRunConfiguration) {
                is OdooRunConfiguration -> (selectedRunConfiguration as? OdooRunConfiguration)?.dbName = value
                is OdooTestConfiguration -> (selectedRunConfiguration as? OdooTestConfiguration)?.dbName = value
            }
        }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dbName = currentDBName ?: return

        if (isRunning.get()) return

        isRunning.set(true)
        service<CommandLineService>().runCommand("dropdb --if-exists $dbName", project) { output ->
            isRunning.set(false)
            ActivityTracker.getInstance().inc() // Triggers the update method
            notify(output, project)
        }
    }

    private fun notify(output: String, project: Project) {
        val dbName = currentDBName ?: return
        val notifier = project.service<OdooBalloonNotifier>()

        if (output.trim() == "NOTICE:  database \"$dbName\" does not exist, skipping") {
            // DB does not exist

            switchRunType(OdooRunType.INIT)
            notifier.notify("Database \"$dbName\" does not exist","Drop $dbName", NotificationType.ERROR)
        } else if (output.contains("database \"$dbName\" is being accessed by other users")) {
            // DB is being accessed

            DB_IS_ACCESSED_RGX.find(output.trim())
                    ?.groupValues
                    ?.drop(1)
                    ?.joinToString(separator = "\n")
                    ?.let { content -> notifier.notify(content, "Drop $dbName", NotificationType.ERROR) }
        } else {
            // Success

            switchRunType(OdooRunType.INIT)
            notifier.notify("Successfully dropped \"$dbName\"", type=NotificationType.INFORMATION)
        }
    }

    private fun switchRunType(runType: OdooRunType) {
        when (selectedRunConfiguration) {
            is OdooRunConfiguration -> (selectedRunConfiguration as? OdooRunConfiguration)?.runType = runType
            is OdooTestConfiguration -> (selectedRunConfiguration as? OdooTestConfiguration)?.runType = runType
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)

        val project = e.project ?: return

        val settings = RunManager.getInstanceIfCreated(project)
                ?.selectedConfiguration
                ?: return

        selectedRunConfiguration = settings.configuration
        if (selectedRunConfiguration !is OdooRunConfiguration && selectedRunConfiguration !is OdooTestConfiguration) {
            e.presentation.isVisible = false
            currentDBName = null
            isRunning.set(false)
            return
        }

        val runningDescriptors = ExecutionManager.getInstance(project).getRunningDescriptors {
            // Filter for run configurations that are currently running on the same database as specified in the currently selected run configuration
            when (val configuration = it.configuration) {
                is OdooRunConfiguration -> configuration.dbName == (selectedDbName ?: "")
                is OdooTestConfiguration -> configuration.dbName == (selectedDbName ?: "")
                else -> false
            }
        }

        // Update isRunning variable according to the currently running processes
        if (isRunning.get() && runningDescriptors.isEmpty()) {
            isRunning.set(false)
        } else if (!isRunning.get() && runningDescriptors.isNotEmpty()) {
            isRunning.set(true)
        }

        e.presentation.isVisible = true
        currentDBName = selectedDbName ?: ""
        e.presentation.text = calcText(selectedDbName ?: "")

        e.presentation.isEnabled = !isRunning.get()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}