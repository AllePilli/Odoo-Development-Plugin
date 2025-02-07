package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.intellij.psi.xml.XmlTag
import kotlin.reflect.KClass

class FieldTypeChooser: DomClassChooser(
        FileTypeField::class,
        TextTypeField::class,
        IntTypeField::class,
        FloatTypeField::class,
        CollectionTypeField::class,
        MarkupTypeField::class,
        ViewArchField::class,
        ListViewArchField::class,
        RefField::class,
        EvalField::class,
        SearchField::class,
        TextField::class,
        RecordField::class,
) {
    override fun chooseClass(tag: XmlTag): KClass<*> = if (tag.hasAttribute("type")) {
        if (tag.getAttributeValue("type") == "xml" && tag.getAttributeValue("name") == "arch") {
            when (tag.subTags.firstOrNull()?.name) {
                "list" -> ListViewArchField::class
                else -> ViewArchField::class
            }
        } else when (tag.getAttributeValue("type")) {
            "int" -> IntTypeField::class
            "float" -> FloatTypeField::class
            "list", "tuple" -> CollectionTypeField::class
            "html", "xml" -> MarkupTypeField::class
            else -> {
                if (tag.hasAttribute("file")) FileTypeField::class
                else TextTypeField::class
            }
        }
    }
    else if (tag.hasAttribute("ref")) RefField::class
    else if (tag.hasAttribute("eval")) EvalField::class
    else if (tag.hasAttribute("search")) SearchField::class
    else if (tag.findFirstSubTag("record") != null) RecordField::class
    else TextField::class

    override fun distinguishTag(tag: XmlTag, klass: KClass<*>) {
        when (klass) {
            IntTypeField::class -> tag.setAttribute("type", "int")
            FloatTypeField::class -> tag.setAttribute("type", "float")
            CollectionTypeField::class -> tag.setAttribute("type", "list")
            MarkupTypeField::class -> tag.setAttribute("type", "html")
            ListViewArchField::class -> distinguishViewArchTag(tag, "list")
            ViewArchField::class -> distinguishViewArchTag(tag)
            TypeField::class -> {
                tag.setAttribute("type", "base64")
                if (klass == FileTypeField::class) tag.setAttribute("file", "some_file")
            }
            RefField::class -> tag.setAttribute("ref", "some_ref")
            EvalField::class -> tag.setAttribute("eval", "1+1")
            SearchField::class -> tag.setAttribute("search", "temp_search")
            RecordField::class -> tag.addSubTag(tag.createChildTag("record", "", "", false), false)
        }
    }

    private fun distinguishViewArchTag(tag: XmlTag, viewType: String? = null) {
        tag.setAttribute("name", "arch")
        tag.setAttribute("type", "xml")

        if (viewType != null) {
            tag.addSubTag(tag.createChildTag(viewType, "", "", false), true)
        }
    }
}