package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.CreateFileAction
import java.util.function.Supplier

class NewXmlFileAction: CreateFileAction(
        StringsBundle.messagePointer("action.NewXmlFileAction.text"),
        StringsBundle.messagePointer("action.NewXmlFileAction.description"),
        Supplier { AllIcons.FileTypes.Xml }
) {
    override fun getDefaultExtension(): String = "xml"
}