package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.ActionNonRootMenuItem
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.TreeNonRootMenuItem

class NonRootMenuItemTypeChooser: AttrDomClassChooser(
        TreeNonRootMenuItem::class,
        "action" to ActionNonRootMenuItem::class,
)