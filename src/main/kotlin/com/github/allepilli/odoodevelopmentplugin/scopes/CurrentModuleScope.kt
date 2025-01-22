package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

class CurrentModuleScope(project: Project, private val currentModule: VirtualFile) : AddonPathsScope(project) {
    override fun getDisplayName(): String = StringsBundle.message("search.scope.current.module.name", currentModule.presentableName)
    override fun contains(file: VirtualFile): Boolean =
            super.contains(file) && VfsUtil.isAncestor(currentModule, file, true)
}