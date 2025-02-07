package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.intellij.util.xml.Attribute
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.Referencing
import com.intellij.util.xml.Required
import com.intellij.util.xml.SubTagList

interface Record: Env {
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>

    @Attribute("forcecreate")
    fun getForceCreate(): GenericAttributeValue<String>

    @Required
    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>

    @SubTagList("field")
    fun getFields(): List<Field>
}