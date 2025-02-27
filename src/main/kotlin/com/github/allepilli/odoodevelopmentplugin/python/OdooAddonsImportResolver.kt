package com.github.allepilli.odoodevelopmentplugin.python

import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.impl.PyImportResolver
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext
import java.io.File

private val odooAddonsPrefix = QualifiedName.fromComponents("odoo", "addons")

class OdooAddonsImportResolver: PyImportResolver {
    /**
     * The default PyCharm implementation struggles finding module imports
     * because odoo puts every module in the odoo.addons directory (during runtime?).
     * This method supplies these imported files that PyCharm cannot find
     */
    override fun resolveImportReference(name: QualifiedName, context: PyQualifiedNameResolveContext, withRoots: Boolean): PsiElement? {
        if (!name.matchesPrefix(odooAddonsPrefix) || name.componentCount <= 2) return null

        // remove the [odooAddonsPrefix]
        val qualifiedModuleName = name.removeHead(2)
        val moduleName = qualifiedModuleName.firstComponent ?: return null
        val module = ModuleDependencyIndexUtil.findModuleByName(context.project, moduleName)
                ?: return null

        val relativePath = qualifiedModuleName
                .removeHead(1)
                .components
                .joinToString(separator = File.separator, postfix = ".py")

        val file = module.findFileByRelativePath(relativePath) ?: return null
        return context.psiManager.findFile(file)
    }
}