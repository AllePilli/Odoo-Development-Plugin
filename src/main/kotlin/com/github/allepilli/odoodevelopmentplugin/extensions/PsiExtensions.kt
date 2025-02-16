package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

val PsiElement.containingModule: VirtualFile?
    get() = containingFile.virtualFile.getContainingModule(project)

fun PsiElement.getText(rangeInElement: TextRange): String? = try {
    buildString {
        append(text, rangeInElement.startOffset, rangeInElement.endOffset)
    }
} catch (e: IndexOutOfBoundsException) {
    logger<PsiElement>().warn(e)
    null
}