package com.github.allepilli.odoodevelopmentplugin.settings.general

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class GeneralSettingsConfigurable(private val project: Project): Configurable {
    private var settingsComponent: GeneralSettingsComponent? = null
    private val settingsState: GeneralSettingsState
        get() = GeneralSettingsState.getInstance(project)

    override fun getDisplayName() = "General Odoo Plugin Settings"
    override fun createComponent(): JComponent? {
        settingsComponent = GeneralSettingsComponent(project)
        return settingsComponent?.panel
    }

    override fun isModified(): Boolean = settingsComponent?.addonPaths != settingsState.addonPaths

    override fun apply() { settingsState.addonPaths = settingsComponent?.addonPaths ?: emptyList() }

    override fun reset() { settingsComponent?.addonPaths = settingsState.addonPaths }

    override fun disposeUIResources() { settingsComponent = null }
}