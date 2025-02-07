package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.IntConverter
import com.intellij.util.xml.*

interface MenuItem: MenuItemAttributes

interface MenuItemAttributes: DomElement {
    @Required
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>

    @Attribute("name")
    fun getName(): GenericAttributeValue<String>

    @Convert(IntConverter::class)
    @Attribute("sequence")
    fun getSequence(): GenericAttributeValue<Int>

    @Attribute("groups")
    fun getGroups(): GenericAttributeValue<String>

    @Attribute("active")
    fun getActive(): GenericAttributeValue<String>
}

interface RootMenuItem: MenuItem {
    @Attribute("web_icon")
    fun getWebIcon(): GenericAttributeValue<String>

    @Attribute("action")
    fun getAction(): GenericAttributeValue<String>

    @SubTagList("menuitem")
    fun getSubMenuItems(): List<SubMenuItem>
}

interface NonRootMenuItem: MenuItem {
    @Required
    @Attribute("parent")
    fun getParentMenuItem(): GenericAttributeValue<String>
}

interface ActionNonRootMenuItem: NonRootMenuItem {
    @Required
    @Attribute("action")
    fun getAction(): GenericAttributeValue<String>
}

interface TreeNonRootMenuItem: NonRootMenuItem {
    @Required
    @SubTagList("menuitem")
    fun getSubMenuItems(): List<SubMenuItem>
}
