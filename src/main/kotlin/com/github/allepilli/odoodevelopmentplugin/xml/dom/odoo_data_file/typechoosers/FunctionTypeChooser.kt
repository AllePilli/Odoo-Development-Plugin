package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.EvalFunction
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.TreeFunction

class FunctionTypeChooser: AttrDomClassChooser(
        TreeFunction::class,
        "eval" to EvalFunction::class
)