package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.intellij.util.xml.*

interface Report: DomElement {
    @Attribute("id")
    fun getId(): GenericAttributeValue<String>

    @Required
    @Attribute("string")
    fun getString(): GenericAttributeValue<String>

    @Required
    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>

    @Required
    @Attribute("name")
    fun getName(): GenericAttributeValue<String>

    @Attribute("print_report_name")
    fun getPrintReportName(): GenericAttributeValue<String>

    @Attribute("report_type")
    fun getReportType(): GenericAttributeValue<String>

    @Attribute("multi")
    fun getMulti(): GenericAttributeValue<String>

    @Attribute("menu")
    fun getMenu(): GenericAttributeValue<String>

    @Attribute("keyword")
    fun getKeyword(): GenericAttributeValue<String>

    @Attribute("file")
    fun getFile(): GenericAttributeValue<String>

    @Attribute("xml")
    fun getXml(): GenericAttributeValue<String>

    @Attribute("parser")
    fun getParser(): GenericAttributeValue<String>

    @Attribute("auto")
    fun getAuto(): GenericAttributeValue<String>

    @Attribute("header")
    fun getHeader(): GenericAttributeValue<String>

    @Attribute("attachment")
    fun getAttachment(): GenericAttributeValue<String>

    @Attribute("attachment_use")
    fun getAttachmentUse(): GenericAttributeValue<String>

    @Attribute("groups")
    fun getGroups(): GenericAttributeValue<String>

    @Attribute("paperformat")
    fun getPaperFormat(): GenericAttributeValue<String>

    @Attribute("usage")
    fun getUsage(): GenericAttributeValue<String>
}