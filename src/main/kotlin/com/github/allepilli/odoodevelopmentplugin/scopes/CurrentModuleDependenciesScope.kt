package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.extensions.findOdooModule
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

class CurrentModuleDependenciesScope(project: Project, private val currentModule: VirtualFile) : AddonPathsScope(project) {
    private val dependencyModules = ModuleDependencyIndexUtil.findAllDependencies(project, currentModule.name)
            .mapNotNull { dependencyName -> project.findOdooModule(dependencyName) }

    override fun getDisplayName(): String =
            StringsBundle.message("search.scope.current.module.dependencies.name", currentModule.presentableName)

    override fun contains(file: VirtualFile): Boolean = super.contains(file)
            && (VfsUtil.isAncestor(currentModule, file, true) || dependencyModules.any { VfsUtil.isAncestor(it, file, true) })
}