package com.github.allepilli.odoodevelopmentplugin.line_markers.override_methods

import com.github.allepilli.odoodevelopmentplugin.InheritanceUtils
import com.github.allepilli.odoodevelopmentplugin.Utils
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction

private val logger = Logger.getInstance(OverrideMethodLineMarkerProvider::class.java)
private const val NOT_A_MODEL_NAME = "__Odoo_Development_Plugin_Not_A_Model_Class__"

class OverrideMethodLineMarkerProvider: RelatedItemLineMarkerProvider(), DumbAware {
    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        var currentModule: VirtualFile? = null
        var currentClassTextRange = -1..-1
        var currentModelName: String = NOT_A_MODEL_NAME

        fun setClass(pyClass: PyClass) {
            currentModule = null
            currentClassTextRange = IntRange(pyClass.textOffset, pyClass.textOffset + pyClass.textLength - 1)
            currentModelName = OdooModelNameIndexUtil.getModelName(pyClass) ?: run {
                logger.warn("Could not find model for ${pyClass.name}")
                NOT_A_MODEL_NAME
            }
        }

        fun tryCollectForMethod(identifier: PsiElement, method: PyFunction) {
            if (currentModelName != NOT_A_MODEL_NAME && currentClassTextRange.first != -1) {
                if (currentModule == null)
                    currentModule = Utils.getContainingModule(method.containingFile)

                if (currentModule != null)
                    collectModelMethodNavigationMarkers(identifier, method, currentClassTextRange, currentModelName, currentModule!!, result)
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

    private fun collectModelMethodNavigationMarkers(identifier: PsiElement, method: PyFunction, classTextRange: IntRange, modelName: String, module: VirtualFile, result: MutableCollection<in LineMarkerInfo<*>>) {
        val project = method.project
        if (!InheritanceUtils.hasParent(project, modelName, module)) return

        val parentModels = InheritanceUtils.getParentModels(project, modelName, module)
        val filePath = identifier.containingFile.virtualFile.path
        val ancestorMethods = parentModels.mapNotNull { parent ->
            // We have to make sure we don't conclude that the model is inheriting from itself otherwise every method
            // in this model will reference back to itself with a line marker
            if (parent.containingFile.virtualFile.path == filePath && parent.textOffset == classTextRange.first) null
            else {
                val parentMethod = parent.findMethodByName(method.name, false, null)
                if (parentMethod == null) null
                else parent to parentMethod
            }
        }


        if (ancestorMethods.isNotEmpty()) {
            val tooltip = if (ancestorMethods.size == 1) {
                val parent = ancestorMethods.single().first
                val moduleOfParent = Utils.getContainingModule(parent.containingFile)?.name
                val parentModelName = OdooModelNameIndexUtil.getModelName(parent)

                "Overrides method of $parentModelName ($moduleOfParent)"
            } else "Overrides multiple methods"

            val builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                    .setTargets(ancestorMethods.map { it.second })
                    .setTooltipText(tooltip)

            result.add(builder.createLineMarkerInfo(identifier))
        }
    }
}