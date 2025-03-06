package com.github.allepilli.odoodevelopmentplugin.line_markers.override_methods

import com.github.allepilli.odoodevelopmentplugin.actions.RunOdooTestAction
import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import org.toml.lang.psi.ext.elementType
import java.io.File

class OdooTestRunLineMarkerContributor: RunLineMarkerContributor() {
    private fun VirtualFile.isTestFile(project: Project): Boolean {
        val path = canonicalPath ?: return false
        val containingAddonPath = project.addonPaths.firstOrNull(path::startsWith) ?: return false
        val modulePath = path.removePrefix("$containingAddonPath${File.separator}")
        val packageInModulePath = modulePath
                .dropWhile { it != File.separatorChar }
                .drop(1)

        return packageInModulePath.takeWhile { it != File.separatorChar } == "tests"
    }

    override fun getInfo(psiElement: PsiElement): Info? {
        if (psiElement.elementType != PyTokenTypes.IDENTIFIER) return null

        return when (val parent = psiElement.parent) {
            is PyFunction, is PyClass -> {
                // exclude functions outside classes
                if (parent is PyFunction && parent.containingClass == null) return null
                if (!parent.containingFile.virtualFile.isTestFile(psiElement.project)) return null

                val actions = arrayOf(RunOdooTestAction(parent.createSmartPointer()))
                Info(AllIcons.RunConfigurations.TestState.Run, actions) {
                    "Run Odoo Test"
                }
            }
            else -> null
        }
    }
}