package com.github.allepilli.odoodevelopmentplugin.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenDBAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.browse("http://localhost:8069/")
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}