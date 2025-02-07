package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.named_enums

import com.intellij.util.xml.NamedEnum

enum class OverloadRelativePositionTypeValueEnum(private val value: String): NamedEnum {
    Before("before"),
    After("after"),
    Inside("inside"),
    Replace("replace"),
    Attributes("attributes"),
    ;

    override fun getValue(): String = value
}