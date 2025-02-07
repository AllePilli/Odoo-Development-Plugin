package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.named_enums

import com.intellij.util.xml.NamedEnum

enum class TypeFieldValueEnum(private val value: String): NamedEnum {
    Base64("base64"),
    Char("char"),
    File("file"),
    Int("int"),
    Float("float"),
    List("list"),
    Tuple("tuple"),
    Html("html"),
    Xml("xml"),
    ;

    override fun getValue(): String = value
}
