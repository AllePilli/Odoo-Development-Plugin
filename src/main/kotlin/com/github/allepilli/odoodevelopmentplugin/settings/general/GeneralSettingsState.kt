package com.github.allepilli.odoodevelopmentplugin.settings.general

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
        name = "com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState",
        storages = [Storage("OdooDevelopmentPlugin.xml")],
)
@Service(Service.Level.PROJECT)
class GeneralSettingsState: PersistentStateComponent<GeneralSettingsState> {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): GeneralSettingsState = project.getService(GeneralSettingsState::class.java)
    }

    var addonPaths = emptyList<String>()
    var defaultTestDbName = "db-test"
    var defaultOdooBinPath = ""

    override fun getState(): GeneralSettingsState = this
    override fun loadState(state: GeneralSettingsState) { XmlSerializerUtil.copyBean(state, this) }
}