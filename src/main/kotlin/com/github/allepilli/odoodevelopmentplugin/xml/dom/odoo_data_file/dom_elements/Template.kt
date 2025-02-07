package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.TemplateReferenceConverter
import com.intellij.util.xml.Attribute
import com.intellij.util.xml.Convert
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue

interface Template: DomElement {
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>

    @Attribute("t-name")
    fun getTName(): GenericAttributeValue<String>

    @Attribute("name")
    fun getName(): GenericAttributeValue<String>

    @Attribute("forcecreate")
    fun getForceCreate(): GenericAttributeValue<String>

    @Attribute("context")
    fun getContext(): GenericAttributeValue<String>

    @Attribute("priority")
    fun getPriority(): GenericAttributeValue<String>

    @Attribute("key")
    fun getKey(): GenericAttributeValue<String>

    @Attribute("website_id")
    fun getWebsiteId(): GenericAttributeValue<String>

    @Attribute("track")
    fun getTrack(): GenericAttributeValue<String>

    @Convert(TemplateReferenceConverter::class, soft = true)
    @Attribute("inherit_id")
    fun getInheritId(): GenericAttributeValue<Template>

    @Attribute("primary")
    fun getPrimary(): GenericAttributeValue<String>

    @Attribute("groups")
    fun getGroups(): GenericAttributeValue<String>

    @Attribute("active")
    fun getActive(): GenericAttributeValue<String>

    @Attribute("customize_show")
    fun getCustomizeShow(): GenericAttributeValue<String>

    // Extra Logic
    fun getContent(): String
}