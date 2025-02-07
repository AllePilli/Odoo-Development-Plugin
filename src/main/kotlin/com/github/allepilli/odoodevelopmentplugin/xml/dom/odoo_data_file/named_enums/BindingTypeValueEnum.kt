package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.named_enums

import com.intellij.util.xml.NamedEnum

enum class BindingTypeValueEnum(private val value: String): NamedEnum {
    Action("action"),
    Report("report"),
    ;

    override fun getValue(): String = value
}