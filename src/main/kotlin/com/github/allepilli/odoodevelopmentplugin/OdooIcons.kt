package com.github.allepilli.odoodevelopmentplugin

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object OdooIcons {
    private const val ICONS_PATH = "/icons"

    private fun getIcon(fileName: String, extension: String = "svg"): Icon =
            IconLoader.getIcon("$ICONS_PATH/$fileName.$extension", javaClass)

    @JvmField
    val odoo = getIcon("odoo_icon")
}