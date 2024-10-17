package com.github.allepilli.odoodevelopmentplugin.test_util

import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.util.io.toNioPathOrNull

object OdooSettingsUtil {
    /**
     * Paths should be absolute canonical paths
     */
    fun setAddonsPaths(project: Project, vararg paths: String) {
        GeneralSettingsState.getInstance(project).addonPaths = paths.map { path ->
            path.toNioPathOrNull()!!.toCanonicalPath()
        }
    }

    /**
     * Paths should be relative to the project's base path
     */
    fun setRelativeAddonsPaths(project: Project, vararg relativePaths: String) {
        val basePath = project.basePath ?: throw IllegalStateException("Project does not have a base path: $project")

        setAddonsPaths(project, *relativePaths.map { "$basePath/$it" }.toTypedArray())
    }
}