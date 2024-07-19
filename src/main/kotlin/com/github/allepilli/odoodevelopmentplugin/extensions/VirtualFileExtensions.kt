package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
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

fun VirtualFile.getAllFiles(fileType: FileType): List<VirtualFile> = processAllFiles(fileType).toList()

fun VirtualFile.processAllFiles(fileType: FileType) = sequence<VirtualFile> {
    if (!isDirectory) throw IllegalArgumentException("This function should only be called on directories, not $this")

    val dirs = ArrayDeque<VirtualFile>().apply {
        addLast(this@processAllFiles)
    }

    while (dirs.isNotEmpty()) {
        val dir = dirs.first()
        val (files, childDirs) = dir.children.partition { it.isFile }

        yieldAll(files.filter { it.fileType == fileType })
        dirs.addAllLast(childDirs)
    }
}