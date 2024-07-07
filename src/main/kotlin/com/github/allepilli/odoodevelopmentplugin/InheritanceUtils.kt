package com.github.allepilli.odoodevelopmentplugin

import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.psi.PyClass
import java.util.logging.Logger

object InheritanceUtils {
    private val log = Logger.getLogger("com.github.allepilli.odoodevelopmentplugin.InheritanceUtils")

    fun hasParent(project: Project, modelName: String, moduleRoot: VirtualFile): Boolean = OdooModelNameIndexUtil
            .getModelInfos(project, modelName, moduleRoot = moduleRoot)
            .firstOrNull()
            ?.let { it.parents.isNotEmpty() } == true

    fun getParentModelNames(project: Project, modelName: String, moduleRoot: VirtualFile): List<String> = OdooModelNameIndexUtil
            .getModelInfos(project, modelName, moduleRoot = moduleRoot)
            .firstOrNull()
            ?.let { it.parents.map { it.first } }
            ?: run {
                log.warning("Did not find the parent of $modelName in context of ${moduleRoot.name}")
                emptyList()
            }

    /**
     * @param modelName model name whose parents we want to find
     * @param moduleRoot the module that this model is part of.
     * @return all possible direct parents of this model in the context of the [moduleRoot]'s dependencies
     */
    fun getParentModels(project: Project, modelName: String, moduleRoot: VirtualFile): List<PyClass> =
            getParentModelNames(project, modelName, moduleRoot).flatMap { parentModelName ->
                OdooModelNameIndexUtil.findModelsByName(project, parentModelName, moduleRoot = moduleRoot)
            }
}