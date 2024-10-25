package com.github.allepilli.odoodevelopmentplugin.inspections.other_quickfixes

import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.github.allepilli.odoodevelopmentplugin.references.ModelNameReference
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiReference
import com.jetbrains.python.inspections.PyUnresolvedReferenceQuickFixProvider
import com.jetbrains.python.psi.PyStringLiteralUtil

/**
 * Quick fix provider for unresolved model names
 *
 * This class adds a quick fix when a [ModelNameReference] does not resolve to anything.
 * @see AddModuleDependencyQuickFix
 */
class UnresolvedOdooModelNameReferenceQuickFixProvider: PyUnresolvedReferenceQuickFixProvider {
    override fun registerQuickFixes(reference: PsiReference, existing: MutableList<LocalQuickFix>) {
        if (reference is ModelNameReference) {
            val element = reference.element
            val project = element.project
            val modelName = PyStringLiteralUtil.getStringValue(element.text)
            val currentModule = element.containingFile.virtualFile.getContainingModule(project)

            if (currentModule == null) {
                logger<UnresolvedOdooModelNameReferenceQuickFixProvider>()
                        .error("Could not find module containing ${element.containingFile.name}")
                return
            }

            // List of module's that contain a model with its name being modelName
            val moduleNames = OdooModelNameIndexUtil.getModelInfos(project, modelName)
                    .mapNotNull { it.moduleName }

            existing.add(AddModuleDependencyQuickFix(currentModule, moduleNames))
        }
    }
}