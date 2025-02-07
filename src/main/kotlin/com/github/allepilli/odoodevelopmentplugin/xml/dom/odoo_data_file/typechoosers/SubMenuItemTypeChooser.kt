package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.ActionSubMenuItem
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.TreeSubMenuItem

class SubMenuItemTypeChooser: AttrDomClassChooser(
        TreeSubMenuItem::class,
        "action" to ActionSubMenuItem::class
)