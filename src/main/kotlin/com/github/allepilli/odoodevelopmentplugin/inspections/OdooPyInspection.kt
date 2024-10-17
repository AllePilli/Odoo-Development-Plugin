package com.github.allepilli.odoodevelopmentplugin.inspections

import ai.grazie.utils.dropPostfix
import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.jetbrains.python.inspections.PyInspection

abstract class OdooPyInspection: PyInspection() {
    override fun getGroupDisplayName(): String = StringsBundle.message("INSP.GROUP.odoo")
    override fun getShortName(): String = super.getShortName().dropPostfix("Inspection")
}