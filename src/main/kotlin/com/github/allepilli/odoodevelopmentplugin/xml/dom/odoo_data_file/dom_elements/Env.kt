package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.intellij.util.xml.Attribute
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue

interface Env: DomElement {
    @Attribute("uid")
    fun getUid(): GenericAttributeValue<String>

    @Attribute("context")
    fun getContext(): GenericAttributeValue<String>
}