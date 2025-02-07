package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.intellij.util.xml.Attribute
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.Required
import com.intellij.util.xml.SubTagList


interface SubMenuItem: MenuItemAttributes

interface ActionSubMenuItem: SubMenuItem {
    @Required
    @Attribute("action")
    fun getAction(): GenericAttributeValue<String>
}

interface TreeSubMenuItem: SubMenuItem {
    @Required
    @SubTagList("menuitem")
    fun getSubMenuItems(): List<SubMenuItem>
}