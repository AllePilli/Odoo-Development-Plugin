package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.EvalValue
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.FileTypeValue
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.SearchValue
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.TextTypeValue

class ValueTypeChooser: AttrDomClassChooser(
        TextTypeValue::class,
        "search" to SearchValue::class,
        "eval" to EvalValue::class,
        "file" to FileTypeValue::class,
)