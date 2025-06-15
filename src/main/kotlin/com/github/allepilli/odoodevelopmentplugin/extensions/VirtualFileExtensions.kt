package com.github.allepilli.odoodevelopmentplugin.extensions

import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import java.io.File

fun VirtualFile.getContainingModuleName(addonPaths: List<String>): String? =
        getContainingModuleNameAndRelativePathInModule(addonPaths)?.first

fun VirtualFile.getContainingModuleNameAndRelativePathInModule(addonPaths: List<String>): Pair<String, String>? {
    if (addonPaths.isEmpty()) throw IllegalArgumentException("addonPaths cannot be empty!")

    return addonPaths.firstOrNull { addonPath -> addonPath in path }
            ?.let { addonPath ->
                path.removePrefix("$addonPath/")
                        .split(File.separatorChar, limit = 2)
                        .takeIf { it.size == 2 }
                        ?.let { (first, second) -> first to second }
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
        val dir = dirs.removeFirst()
        val (files, childDirs) = dir.children.partition { it.isFile }

        yieldAll(files.filter { it.fileType == fileType })
        dirs.addAllLast(childDirs)
    }
}

/**
 * Checks if the virtual file is an odoo module directory. This function will only return true if the virtual file
 * is a direct child directory of one of the configured addon paths.
 * @param project the project to check inside, if null, this function will check inside all the projects this virtual file is part of
 * @see Project.addonPaths
 * @see ProjectLocator.getProjectsForFile
 */
fun VirtualFile.isOdooModuleDirectory(project: Project? = null): Boolean {
    if (!isDirectory) return false
    val addonPathsToCheck = project?.addonPaths ?: ProjectLocator.getInstance()
            .getProjectsForFile(this)
            .flatMapNotNull { it?.addonPaths }
            .toSet()

    return addonPathsToCheck.any { path -> canonicalPath == "$path${File.separatorChar}$name" }
}

fun VirtualFile.getRootElements(project: Project): List<OdooOpenerpData> {
    if (!isDirectory) return emptyList()

    val psiManager = PsiManager.getInstance(project)
    val domManager = DomManager.getDomManager(project)

    return getAllFiles(XmlFileType.INSTANCE)
            .mapNotNull { psiManager.findFile(it) as? XmlFile? }
            .mapNotNull { domManager.getFileElement(it, OdooOpenerpData::class.java)?.rootElement }
}

fun VirtualFile.getRelativePathInModule(project: Project): String? {
    val modulePath = getContainingModule(project)?.canonicalPath ?: return null
    return canonicalPath?.replaceFirst(modulePath, "")
}