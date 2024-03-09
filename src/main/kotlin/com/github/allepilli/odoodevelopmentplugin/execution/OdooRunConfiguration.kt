package com.github.allepilli.odoodevelopmentplugin.execution

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.run.AbstractPythonRunConfiguration
import java.io.File

class OdooRunConfiguration(project: Project, factory: ConfigurationFactory) : AbstractPythonRunConfiguration<OdooRunConfiguration>(project, factory) {
    init {
        setUnbufferedEnv()
    }

    var runType: OdooRunType
        get() = options.runType
        set(value) { options.runType = value }
    var odooBinPath: String
        get() = options.odooBinPath ?: ""
        set(value) { options.odooBinPath = value }
    var dbName: String
        get() = options.dbName ?: ""
        set(value) { options.dbName = value }
    var addonsPaths: String
        get() = options.addonsPaths ?: ""
        set(value) { options.addonsPaths = value }
    var odooModules: String
        get() = options.odooModules ?: ""
        set(value) { options.odooModules = value}
    var otherOptions: String
        get() = options.otherOptions ?: ""
        set(value) { options.otherOptions = value }

    override fun getOptions(): OdooRunConfigurationOptions = super.getOptions() as OdooRunConfigurationOptions
    override fun createConfigurationEditor(): SettingsEditor<OdooRunConfiguration> = OdooRunConfigurationSettingsEditor(project)
    override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState = OdooRunCommandLineState(this, env)
    override fun getType(): ConfigurationType = OdooConfigurationType.getInstance()
    override fun checkConfiguration() {
        super.checkConfiguration()
        checkRunType()
        checkOdooBinPath()
        checkDBName()
    }

    private fun checkRunType() {
        if (runType == OdooRunType.NONE) throw RuntimeConfigurationError("Please choose a run type")
    }

    private fun checkOdooBinPath() {
        if (odooBinPath.isBlank() || odooBinPath.isEmpty()) throw RuntimeConfigurationError("Please specify the path to the odoo-bin file")
        val file = File(odooBinPath)

        if (!file.exists()) throw RuntimeConfigurationError("The odoo-bin path does not point to an existing file")
        if (!file.isFile) throw RuntimeConfigurationError("The odoo-bin path does not point to a file")
        if (file.name != "odoo-bin") throw RuntimeConfigurationError("The odoo-bin path should point to a file named 'odoo-bin'")

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: throw RuntimeConfigurationError("Could not find file pointed to by odoo-bin path")
        if (virtualFile.fileType != PythonFileType.INSTANCE) throw RuntimeConfigurationError("The odoo-bin path does not point to a Python file")
    }

    private fun checkDBName() {
        if (dbName.isEmpty() || dbName.isBlank()) throw RuntimeConfigurationError("Please specify the database name")
        if (' ' in dbName) throw RuntimeConfigurationError("Database name should not contain spaces")
    }
}