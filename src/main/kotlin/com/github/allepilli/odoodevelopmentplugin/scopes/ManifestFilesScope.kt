package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ManifestFilesScope(project: Project) : AddonPathsScope(project) {
    override fun getDisplayName(): String = StringsBundle.message("search.scope.manifest.files.name")
    override fun contains(file: VirtualFile): Boolean =
            file.name == Constants.MANIFEST_FILE_WITH_EXT && super.contains(file)
}