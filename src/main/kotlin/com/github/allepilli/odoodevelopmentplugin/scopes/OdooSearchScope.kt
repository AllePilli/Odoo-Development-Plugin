package com.github.allepilli.odoodevelopmentplugin.scopes

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import javax.swing.Icon

abstract class OdooSearchScope(project: Project? = null): GlobalSearchScope(project) {
    override fun getIcon(): Icon = OdooIcons.odoo
    override fun isSearchInModuleContent(aModule: Module): Boolean = false
    override fun isSearchInLibraries(): Boolean = false
}