package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.implementations

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Template

abstract class TemplateImpl: Template {
    override fun getContent(): String = this.xmlTag?.value?.text ?: ""
}