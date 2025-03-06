package com.github.allepilli.odoodevelopmentplugin.execution

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions

class OdooRunConfigurationOptions: ModuleBasedConfigurationOptions() {
    var runType by property(OdooRunType.INIT) { it == OdooRunType.NONE }
    var odooBinPath by string("")
    var dbName by string("")
    var addonsPaths by string("")
    var odooModules by string("")
    var otherOptions by string("")
}