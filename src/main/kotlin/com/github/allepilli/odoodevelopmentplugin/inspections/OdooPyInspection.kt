package com.github.allepilli.odoodevelopmentplugin.inspections

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.dropPostfix
import com.jetbrains.python.inspections.PyInspection

abstract class OdooPyInspection: PyInspection() {
    override fun getGroupDisplayName(): String = StringsBundle.message("INSP.GROUP.odoo")
    override fun getShortName(): String = super.getShortName().dropPostfix("Inspection")
}