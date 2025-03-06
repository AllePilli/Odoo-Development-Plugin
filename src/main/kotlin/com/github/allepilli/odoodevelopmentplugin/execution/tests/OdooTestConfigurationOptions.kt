package com.github.allepilli.odoodevelopmentplugin.execution.tests

import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunType
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions

class OdooTestConfigurationOptions: ModuleBasedConfigurationOptions() {
    var runType by property(OdooRunType.INIT) { it == OdooRunType.NONE }
    var odooBinPath by string("")
    var dbName by string("")
    var addonsPaths by string("")
    var odooModules by string("")
    var otherOptions by string("")
    var tag by string("")
    var testModule by string("")
    var testClass by string("")
    var testMethod by string("")
    var stopAfterInit by property(true)
}