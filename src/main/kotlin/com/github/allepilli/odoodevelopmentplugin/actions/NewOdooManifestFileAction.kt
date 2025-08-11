package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.dialogs.ManifestFormDialog
import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.WriteActionAware
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager

class NewOdooManifestFileAction: AnAction(), WriteActionAware {
    companion object {
        private const val PROP_NAME = "MODULE_NAME"
        private const val PROP_VERSION = "MODULE_VERSION"
        private const val PROP_CATEGORY = "MODULE_CATEGORY"
        private const val PROP_DESCRIPTION = "MODULE_DESCRIPTION"
        private const val PROP_LICENSE = "MODULE_LICENSE"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val dir = view.orChooseDirectory ?: return

        with(ManifestFormDialog(project)) {
            if (showAndGet()) {
                val template = FileTemplateManager.getInstance(project).getJ2eeTemplate("Manifest")
                createManifestFile(template, dir, extraTemplateProperties = mapOf(
                        PROP_NAME to (name.trim().takeUnless(String::isEmpty) ?: "Module Name"),
                        PROP_VERSION to (version.trim().takeUnless(String::isEmpty) ?: "1.0"),
                        PROP_CATEGORY to category.trim(),
                        PROP_DESCRIPTION to description.trim(),
                        PROP_LICENSE to license.trim()
                ))
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val dataContext = e.dataContext
        val psiDirectory = CommonDataKeys.PSI_ELEMENT.getData(dataContext) as? PsiDirectory ?: return

        e.presentation.isEnabledAndVisible = psiDirectory.findFile(Constants.MANIFEST_FILE_WITH_EXT) == null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private fun createManifestFile(
            template: FileTemplate,
            directory: PsiDirectory,
            openFile: Boolean = true,
            extraTemplateProperties: Map<String, String> = emptyMap(),
            liveTemplateDefaultValues: Map<String, String> = emptyMap(),
            defaultTemplateProperty: String? = null
    ): PsiFile? {
        var dir: PsiDirectory
        val name = with(CreateFileAction.MkDirs(Constants.MANIFEST_FILE_NAME, directory)) {
            dir = this.directory
            newName
        }

        val project = dir.project
        val properties = FileTemplateManager.getInstance(project).defaultProperties.apply {
            putAll(extraTemplateProperties)
        }

        val psiFile = FileTemplateUtil.createFromTemplate(template, name, properties, dir) as PsiFile
        val pointer = SmartPointerManager.getInstance(project)
                .createSmartPsiElementPointer(psiFile)

        return psiFile.virtualFile?.let { virtualFile ->
            if (openFile) {
                if (template.isLiveTemplateEnabled) CreateFromTemplateManager.startLiveTemplate(psiFile, liveTemplateDefaultValues)
                else FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }

            defaultTemplateProperty?.let {
                PropertiesComponent.getInstance(project).setValue(it, template.name)
            }

            pointer.element as PsiFile
        }
    }
}
