package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

val PsiElement.containingModule: VirtualFile?
    get() = containingFile.virtualFile.getContainingModule(project)