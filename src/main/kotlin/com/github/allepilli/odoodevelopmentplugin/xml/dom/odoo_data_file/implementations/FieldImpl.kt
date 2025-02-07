package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.implementations

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.MarkupTypeField

abstract class MarkupTypeFieldImpl: MarkupTypeField {
    override fun getCode(): String = this.xmlTag?.value?.text ?: ""
}