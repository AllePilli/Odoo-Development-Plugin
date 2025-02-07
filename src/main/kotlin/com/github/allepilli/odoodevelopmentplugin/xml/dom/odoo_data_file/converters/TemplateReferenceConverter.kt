package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Template

class TemplateReferenceConverter: RefReferenceConverter<Template>() {
    override fun getId(any: Template): String? = any.getId().value
    override fun getCandidateElements(root: OdooOpenerpData): List<Template> = root.getTemplatesRec()
}