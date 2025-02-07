package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Delete
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.IdDelete
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.SearchDelete

class DeleteTypeChooser : AttrDomClassChooser(
        Delete::class,
        "search" to SearchDelete::class,
        "id" to IdDelete::class
)