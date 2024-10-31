package com.github.allepilli.odoodevelopmentplugin

import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction

object InheritanceUtils {
    fun getParentModels(project: Project, contextModule: VirtualFile, modelName: String, module: String = contextModule.name, addonPaths: List<String>): List<PyClass> {
        val item = OdooModelNameIndexUtil.getModelInfo(project, modelName, module, addonPaths) ?: return emptyList()
        val checkedParents = mutableSetOf<String>()
        var parents = item.parents.map { it.name }.toSet()

        do {
            val diff = if (checkedParents.isEmpty()) {
                val temp = (parents - checkedParents)
                checkedParents.addAll(parents)
                temp
            } else (parents - checkedParents)

            parents = diff.flatMap { parentName ->
                OdooModelNameIndexUtil.getModelInfos(project, parentName, contextModule).map { it.parents.map { it.name } }
            }.flatten().toSet()
            checkedParents.addAll(parents)
        } while (parents.isNotEmpty())

        return checkedParents.flatMap { parent ->
            OdooModelNameIndexUtil.findModelsByName(project, parent, contextModule)
        }
    }

    fun getAllInheritedMethods(project: Project, modelName: String, contextModule: VirtualFile, modelClass: PyClass): List<PyFunction> =
            getParentModels(project, contextModule, modelName, addonPaths = project.addonPaths)
                    .filterNot { it == modelClass }
                    .flatMap { parentClass -> parentClass.methods.toList() }
}