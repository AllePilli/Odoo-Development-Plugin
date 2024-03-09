package com.github.allepilli.odoodevelopmentplugin.execution

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class OdooRunConfigurationFactory: ConfigurationFactory(OdooConfigurationType.getInstance()) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration = OdooRunConfiguration(project, this)
    override fun getOptionsClass(): Class<out BaseState> = OdooRunConfigurationOptions::class.java
    override fun getId(): String = "OdooRunConfigurationFactory"
}