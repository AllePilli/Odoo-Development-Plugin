package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfiguration
import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfigurationFactory
import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction

class RunOdooTestAction(private val runElement: SmartPsiElementPointer<PsiElement>) : AbstractRunOdooTestAction() {
    override fun createSettings(e: AnActionEvent): RunnerAndConfigurationSettings? {
        val runManager = RunManager.getInstance(e.project ?: return null)
        return when (val element = runElement.element ?: return null) {
            is PyFunction -> createTestMethodSettings(runManager, element)
            is PyClass -> createTestClassSettings(runManager, element)
            else -> null
        }
    }

    private fun createTestMethodSettings(runManager: RunManager, method: PyFunction): RunnerAndConfigurationSettings? {
        val pyClass = method.containingClass ?: return null
        val module = pyClass.containingModule?.name ?: return null
        val testClassName = pyClass.name ?: return null
        val testMethodName = method.name ?: return null
        val testConfigName = "$testClassName.$testMethodName"

        val runConfSettings =
                runManager.createConfiguration(testConfigName, OdooTestConfigurationFactory())

        (runConfSettings.configuration as? OdooTestConfiguration ?: return null).apply {
            odooModules = module
            testModule = module
            testClass = testClassName
            testMethod = testMethodName
        }

        return runConfSettings
    }

    private fun createTestClassSettings(runManager: RunManager, pyClass: PyClass): RunnerAndConfigurationSettings? {
        val module = pyClass.containingModule?.name ?: return null
        val testClassName = pyClass.name ?: return null

        val runConfSettings =
                runManager.createConfiguration(testClassName, OdooTestConfigurationFactory())

        (runConfSettings.configuration as? OdooTestConfiguration ?: return null).apply {
            odooModules = module
            testModule = module
            testClass = testClassName
        }

        return runConfSettings
    }
}