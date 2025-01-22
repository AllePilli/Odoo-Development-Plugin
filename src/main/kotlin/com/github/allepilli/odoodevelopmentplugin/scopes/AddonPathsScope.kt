package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

open class AddonPathsScope(project: Project): OdooSearchScope(project) {
    override fun getDisplayName(): String = StringsBundle.message("search.scope.addons.paths.name")
    override fun contains(file: VirtualFile): Boolean =
            project?.addonPaths?.any { file.canonicalPath?.startsWith(it) == true } == true
}