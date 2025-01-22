package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class LocalizationModulesScope(project: Project) : AddonPathsScope(project) {
    override fun getDisplayName(): String = StringsBundle.message("search.scope.localization.modules.name")
    override fun contains(file: VirtualFile): Boolean = super.contains(file) && "l10n_" in (file.canonicalPath ?: "")
}