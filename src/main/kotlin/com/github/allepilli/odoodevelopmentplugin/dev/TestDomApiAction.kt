package com.github.allepilli.odoodevelopmentplugin.dev

import com.github.allepilli.odoodevelopmentplugin.isOdooDataFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.xml.XmlFile

class TestDomApiAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = PsiDocumentManager.getInstance(project)
                .getPsiFile(editor.document)
                as? XmlFile
                ?: return

        if (!file.isOdooDataFile) return

        OdooDomViewerDialog(project, editor).show()
    }
}