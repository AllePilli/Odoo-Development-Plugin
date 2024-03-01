package com.github.allepilli.odoodevelopmentplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

inline fun <reified E> buildArray(builderAction: MutableList<E>.() -> Unit): Array<E> = buildList(builderAction).toTypedArray()

fun findModule(name: String, project: Project): VirtualFile? {
    val virtualFileManager = VirtualFileManager.getInstance()
    val basePath = project.basePath ?: return null

    return virtualFileManager.findFileByUrl("file://$basePath/odoo/addons/$name")
            ?: virtualFileManager.findFileByUrl("file://$basePath/enterprise/$name")
}