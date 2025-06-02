package com.github.allepilli.odoodevelopmentplugin.line_markers.override_methods

import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.*
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction

private val logger = Logger.getInstance(OverrideMethodLineMarkerProvider::class.java)
private const val NOT_A_MODEL_NAME = "__Odoo_Development_Plugin_Not_A_Model_Class__"

class OverrideMethodLineMarkerProvider: RelatedItemLineMarkerProvider() {
    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val addonPaths = elements.firstOrNull()?.project?.addonPaths ?: return
        var currentModule: VirtualFile?
        var currentClassTextRange = -1..-1
        var currentModelName: String
        var currentInheritedMethods: List<PyFunction> = listOf()

        fun setClass(pyClass: PyClass) {
            currentModule = null
            currentClassTextRange = IntRange(pyClass.textOffset, pyClass.textOffset + pyClass.textLength - 1)
            currentModelName = OdooModelNameIndexUtil.getModelName(pyClass) ?: run {
                if (pyClass.isOdooModel(includeBaseModels = false))
                    logger.warn("Could not find model for ${pyClass.name} ${pyClass.containingFile.virtualFile.path}")
                NOT_A_MODEL_NAME
            }

            currentModule = if (currentModelName != NOT_A_MODEL_NAME) {
                pyClass.containingFile.virtualFile.getContainingModule(pyClass.project, addonPaths)
            } else null

            currentInheritedMethods = if (currentModelName != NOT_A_MODEL_NAME && currentModule != null) {
                currentModule?.name?.let { moduleName ->
                    val model = Model(pyClass.project, currentModelName, moduleName)
                    model.getInheritedMethods(pyClass)
                } ?: listOf()
            } else listOf()
        }

        fun tryCollectForMethod(identifier: PsiElement, method: PyFunction) {
            if (currentInheritedMethods.isNotEmpty()) {
                collectModelMethodNavigationMarkers(identifier, method, currentInheritedMethods, result)
            }
        }

        for (element in elements) {
            if (element.elementType != PyTokenTypes.IDENTIFIER) continue

            if (element.parent is PyClass) {
                setClass(element.parent as PyClass)
            } else if (element.parent is PyFunction) {
                (element.parent as PyFunction).asMethod()?.let { method ->
                    if (currentClassTextRange.first == -1) {
                        // set class info
                        method.containingClass?.let { pyClass ->
                            setClass(pyClass)
                            tryCollectForMethod(element, method)
                        }
                    } else {
                        if (method.textOffset in currentClassTextRange) {
                            tryCollectForMethod(element, method)
                        } else {
                            // The method is in another class
                            method.containingClass?.let { pyClass ->
                                setClass(pyClass)
                                tryCollectForMethod(element, method)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun collectModelMethodNavigationMarkers(identifier: PsiElement, method: PyFunction, inheritedMethods: List<PyFunction>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val overriddenMethods = inheritedMethods
                .filter { parentMethod -> parentMethod.name == method.name }
                .takeUnlessEmpty()
                ?: return

        val tooltip = if (overriddenMethods.size == 1) {
            val parentClass = overriddenMethods.single().containingClass!!
            val parentModuleName = parentClass.containingFile.virtualFile.getContainingModule(method.project)?.name
            val parentModelName = parentClass.getModelName()

            "Overrides method of $parentModelName ($parentModuleName)"
        } else "Overrides multiple methods"

        result.add(
                NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                        .setTargets(overriddenMethods)
                        .setTooltipText(tooltip)
                        .createLineMarkerInfo(identifier)
        )
    }
}