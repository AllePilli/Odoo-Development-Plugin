package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.ActionNonRootMenuItem
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.RootMenuItem
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.TreeNonRootMenuItem
import com.intellij.psi.xml.XmlTag
import kotlin.reflect.KClass

class MenuItemTypeChooser: DomClassChooser(
        ActionNonRootMenuItem::class,
        TreeNonRootMenuItem::class,
        RootMenuItem::class,
) {
    override fun chooseClass(tag: XmlTag): KClass<*> = if (tag.hasAttribute("parent")) {
        if (tag.hasAttribute("action")) ActionNonRootMenuItem::class
        else TreeNonRootMenuItem::class
    } else RootMenuItem::class

    override fun distinguishTag(tag: XmlTag, klass: KClass<*>) {
        when (klass) {
            ActionNonRootMenuItem::class -> {
                tag.setAttribute("parent", "temp_parent")
                tag.setAttribute("action", "temp_action")
            }
            TreeNonRootMenuItem::class -> tag.setAttribute("parent", "temp_parent")
        }
    }
}