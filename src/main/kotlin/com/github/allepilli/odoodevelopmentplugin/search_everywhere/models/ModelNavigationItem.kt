package com.github.allepilli.odoodevelopmentplugin.search_everywhere.models

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModuleName
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FindSymbolParameters
import javax.swing.Icon

class ModelNavigationItem(val modelName: String, val offset: Int, val file: VirtualFile, parameters: FindSymbolParameters): NavigationItem {
    val project = parameters.project
    var moduleName: String? = null
        private set

    init {
        ApplicationManager.getApplication().runReadAction {
            moduleName = file.getContainingModuleName(project.addonPaths)
        }
    }

    override fun getName(): String = modelName
    override fun canNavigate(): Boolean = true
    override fun canNavigateToSource(): Boolean = true
    override fun navigate(requestFocus: Boolean) = OpenFileDescriptor(project, file, offset).navigate(requestFocus)

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String = modelName
        override fun getIcon(unused: Boolean): Icon = OdooIcons.odoo
        override fun getLocationString(): String = "($moduleName)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelNavigationItem

        if (modelName != other.modelName) return false
        if (offset != other.offset) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelName.hashCode()
        result = 31 * result + offset
        result = 31 * result + file.hashCode()
        return result
    }
}