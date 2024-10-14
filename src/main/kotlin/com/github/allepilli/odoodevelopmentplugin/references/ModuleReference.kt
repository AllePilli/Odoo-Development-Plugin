package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.directories
import com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsState
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.python.psi.PyStringLiteralUtil
import java.io.File

class ModuleReference(psiElement: PsiElement, rangeInElement: TextRange): PsiReferenceBase<PsiElement>(psiElement, rangeInElement) {
    override fun resolve(): PsiElement? {
        val moduleName = PyStringLiteralUtil.getStringValue(element.text)
        val virtualFileManager = VirtualFileManager.getInstance()
        var module: VirtualFile? = null

        for (addonPath in GeneralSettingsState.getInstance(element.project).addonPaths) {
            val addonDir = virtualFileManager.findFileByNioPath(File(addonPath).toPath()) ?: continue
            module = addonDir.findDirectory(moduleName)

            if (module != null) break
        }

        if (module == null) return null

        return PsiManager.getInstance(element.project).findDirectory(module)
    }

    override fun getVariants(): Array<Any> {
        val psiManager = PsiManager.getInstance(element.project)
        val virtualFileManager = VirtualFileManager.getInstance()

        return GeneralSettingsState.getInstance(element.project).addonPaths
                .asSequence()
                .map { File(it).toPath() }
                .mapNotNull { virtualFileManager.findFileByNioPath(it) }
                .flatMap { it.directories }
                .mapNotNull { vf -> psiManager.findDirectory(vf) }
                .map { module -> LookupElementBuilder.create(module).withIcon(OdooIcons.odoo) }
                .toList()
                .toTypedArray()
    }
}