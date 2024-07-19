package com.github.allepilli.odoodevelopmentplugin.extensions

import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

/**
 * @see [GeneralSettingsState.addonPaths]
 */
val Project.addonPaths: List<String>
    get() = GeneralSettingsState.getInstance(this).addonPaths

/**
 * Searches all addon directories @see [Project.addonPaths] for a module by [name]
 */
fun Project.findOdooModule(name: String): VirtualFile? {
    val virtualFileManager = VirtualFileManager.getInstance()

    for (path in addonPaths) {
        val virtualFile = virtualFileManager.findFileByUrl("file://$path/$name") ?: continue

        return virtualFile
    }

    return null
}