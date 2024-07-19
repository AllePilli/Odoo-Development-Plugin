package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun VirtualFile.getContainingModuleName(addonPaths: List<String>): String? {
    if (addonPaths.isEmpty()) throw IllegalArgumentException("addonPaths cannot be empty!")

    return addonPaths.firstOrNull { addonPath -> addonPath in path }
            ?.let { addonPath ->
                path.removePrefix("$addonPath/")
                        .split(File.separatorChar, limit = 2)
                        .firstOrNull()
            }
}

fun VirtualFile.getContainingModule(project: Project, addonPaths: List<String> = project.addonPaths): VirtualFile? {
    val moduleName = getContainingModuleName(addonPaths) ?: kotlin.run {
        Logger.getInstance("PSI").warn("Could not find module name for ${this.name}")
        return null
    }

    return project.findOdooModule(moduleName) ?: kotlin.run {
        Logger.getInstance("PSI").warn("Could not find module for ${this.name}")
        null
    }
}