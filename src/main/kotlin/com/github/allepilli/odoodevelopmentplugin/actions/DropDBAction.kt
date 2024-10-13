package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunConfiguration
import com.github.allepilli.odoodevelopmentplugin.notifications.OdooBalloonNotifier
import com.github.allepilli.odoodevelopmentplugin.services.CommandLineService
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.ide.ActivityTracker
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
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
            notifier.notify("Successfully dropped \"$dbName\"", type=NotificationType.INFORMATION)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project ?: return

        val settings = RunManager.getInstanceIfCreated(project)
                ?.selectedConfiguration
                ?: return

        val selectedRunConfiguration = settings.configuration

        if (!isRunning.get() && selectedRunConfiguration is OdooRunConfiguration) {
            ExecutionManager.getInstance(project)?.getRunningDescriptors { it == settings }?.let {
                // If the current run configuration is running
                if (it.isNotEmpty()) isRunning.set(true)
            }
        }

        if (selectedRunConfiguration is OdooRunConfiguration) {
            e.presentation.isVisible = true
            currentDBName = selectedRunConfiguration.dbName
            e.presentation.text = calcText(selectedRunConfiguration.dbName)

            e.presentation.isEnabled = !isRunning.get()
        } else {
            e.presentation.isVisible = false
            currentDBName = null
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun displayTextInToolbar(): Boolean = true
}