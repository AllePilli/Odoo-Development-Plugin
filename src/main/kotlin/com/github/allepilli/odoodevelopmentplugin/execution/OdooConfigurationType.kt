package com.github.allepilli.odoodevelopmentplugin.execution

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import javax.swing.Icon

class OdooConfigurationType: ConfigurationType {
    companion object {
        @JvmStatic
        fun getInstance() = ConfigurationTypeUtil.findConfigurationType(OdooConfigurationType::class.java)
    }

    override fun getDisplayName(): String = "Odoo"
    override fun getConfigurationTypeDescription(): String = "Run configurations for Odoo development"
    override fun getIcon(): Icon = OdooIcons.odoo
    override fun getId(): String = "OdooConfigurationType"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(OdooRunConfigurationFactory())
}