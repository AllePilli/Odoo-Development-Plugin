package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.intellij.util.xml.*

interface Delete: DomElement {
    @Required
    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>
}

interface IdDelete: Delete {
    @Required
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>
}

interface SearchDelete: Delete {
    @Required
    @Attribute("search")
    fun getSearch(): GenericAttributeValue<String>
}