package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.named_enums.BindingTypeValueEnum
import com.intellij.util.xml.Attribute
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.Referencing
import com.intellij.util.xml.Required

interface ActWindow: DomElement {
    @Required
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>

    @Required
    @Attribute("name")
    fun getName(): GenericAttributeValue<String>

    @Required
    @Referencing(ModelReferenceConverter::class, soft = true)
    @Attribute("res_model")
    fun getResModel(): GenericAttributeValue<String>

    @Attribute("domain")
    fun getDomain(): GenericAttributeValue<String>

    @Attribute("context")
    fun getContext(): GenericAttributeValue<String>

    @Attribute("view_id")
    fun getViewId(): GenericAttributeValue<String>

    @Attribute("view_mode")
    fun getViewMode(): GenericAttributeValue<String>

    @Attribute("target")
    fun getTarget(): GenericAttributeValue<String>

    @Attribute("groups")
    fun getGroups(): GenericAttributeValue<String>

    @Attribute("limit")
    fun getLimit(): GenericAttributeValue<String>

    @Attribute("usage")
    fun getUsage(): GenericAttributeValue<String>

    @Attribute("binding_model")
    fun getBindingModel(): GenericAttributeValue<String>

    @Attribute("binding_type")
    fun getBindingType(): GenericAttributeValue<BindingTypeValueEnum>

    @Attribute("binding_views")
    fun getBindingViews(): GenericAttributeValue<String>
}