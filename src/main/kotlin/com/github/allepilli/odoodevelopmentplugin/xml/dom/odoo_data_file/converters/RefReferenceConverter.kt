package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.extensions.findOdooModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getRootElements
import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.intellij.openapi.vfs.originalFile
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.ResolvingConverter

abstract class RefReferenceConverter<T: DomElement>: ResolvingConverter<T>() {
    abstract fun getId(any: T): String?
    abstract fun getCandidateElements(root: OdooOpenerpData): List<T>

    override fun toString(any: T?, context: ConvertContext): String? {
        if (any == null) return null

        val id = getId(any) ?: return null
        val recordModule = any.xmlElement?.containingFile?.virtualFile?.getContainingModule(context.project) ?: return null
        val containingModule = context.file.viewProvider.virtualFile.originalFile() ?: return null

        return if (recordModule.name != containingModule.name) "${recordModule.presentableName}.$id" else id
    }

    override fun fromString(string: String?, context: ConvertContext): T? {
        if (string == null) return null

        if ('.' in string) {
            val (moduleName, id) = string.split('.')
                    .takeIf { it.size == 2 }
                    ?: return null

            val module = context.project.findOdooModule(moduleName) ?: return null
            val roots = module.getRootElements(context.project)

            for (candidate in roots) {
                val elements = getCandidateElements(candidate)
                for (element in elements) {
                    if (getId(element) == id) return element
                }
            }
        } else {
            val roots = context.file.virtualFile
                    .getContainingModule(context.project)
                    ?.getRootElements(context.project)
                    ?: return null

            for (candidate in roots) {
                val elements = getCandidateElements(candidate)
                for (element in elements) {
                    if (getId(element) == string) return element
                }
            }
        }

        return null
    }

    override fun getVariants(context: ConvertContext): Collection<T> {
        val project = context.project
        val file = context.file.viewProvider.virtualFile.originalFile() ?: return emptyList()
        val dependencies = listOf(file.getContainingModule(project)) +
                ModuleDependencyIndexUtil.findAllDependenciesFiles(project, file)

        return dependencies.flatMapNotNull { module ->
            module?.getRootElements(project)
                    ?.flatMap(::getCandidateElements)
        }
    }
}