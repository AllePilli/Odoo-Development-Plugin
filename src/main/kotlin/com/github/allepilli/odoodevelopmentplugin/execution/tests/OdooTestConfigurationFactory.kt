package com.github.allepilli.odoodevelopmentplugin.execution.tests

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.execution.OdooConfigurationType
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class OdooTestConfigurationFactory: ConfigurationFactory(OdooConfigurationType.Util.getInstance()) {
    override fun getId(): String = "OdooTestRunConfigurationFactory"
    override fun createTemplateConfiguration(project: Project): RunConfiguration = OdooTestConfiguration(project, this)
    override fun getOptionsClass(): Class<out BaseState> = OdooTestConfigurationOptions::class.java
    override fun getName(): String = StringsBundle.message("run.configuration.factory.test.name")
}