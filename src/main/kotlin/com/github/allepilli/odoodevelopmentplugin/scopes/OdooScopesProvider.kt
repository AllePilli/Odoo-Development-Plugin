package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.SearchScopeProvider

class OdooScopesProvider: SearchScopeProvider {
    override fun getDisplayName(): String = StringsBundle.message("search.scope.provider.odoo.name")
    override fun getSearchScopes(project: Project, dataContext: DataContext): MutableList<SearchScope> {
        if (project.addonPaths.isEmpty()) return mutableListOf()
        val scopes = mutableListOf<SearchScope>(
                AddonPathsScope(project),
                ManifestFilesScope(project),
                LocalizationModulesScope(project),
        )

        // TODO handle case where the virtual file is a directory
        dataContext.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isFile }?.let { virtualFile ->
            virtualFile.getContainingModule(project)?.let { containingModule ->
                scopes.add(CurrentModuleScope(project, containingModule))
                scopes.add(CurrentModuleDependenciesScope(project, containingModule))
            }
        }

        return scopes
    }
}