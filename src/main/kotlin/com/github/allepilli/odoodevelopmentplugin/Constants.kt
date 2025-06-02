package com.github.allepilli.odoodevelopmentplugin

object Constants {
    const val MANIFEST_FILE_NAME = "__manifest__"
    const val MANIFEST_FILE_WITH_EXT = "$MANIFEST_FILE_NAME.py"

    val ODOO_DATA_FILE_ROOTS = setOf("odoo", "data", "openerp")
    val ODOO_BASE_MODELS_FQN = setOf(
            "odoo.models.Model",
            "odoo.models.BaseModel",
            "odoo.models.AbstractModel",
            "odoo.models.TransientModel",
    )
}