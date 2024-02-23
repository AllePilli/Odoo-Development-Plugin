package com.github.allepilli.odoodevelopmentplugin.templates

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class OdooFileTemplatesGroupFactory: FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor = FileTemplateGroupDescriptor(
            StringsBundle.message("template.OdooFileTemplatesGroupFactory.title"),
            OdooIcons.odoo
    ).apply {
        addTemplate(
                FileTemplateDescriptor(
                        StringsBundle.message("template.OdooFileTemplatesGroupFactory.name.manifest"),
                        OdooIcons.odoo
                )
        )
    }
}