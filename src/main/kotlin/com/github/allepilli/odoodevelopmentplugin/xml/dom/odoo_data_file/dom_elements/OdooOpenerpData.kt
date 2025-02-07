package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.intellij.util.xml.*

interface OdooOpenerpData: Env {
    @Attribute("noupdate")
    fun getNoUpdate(): GenericAttributeValue<String>

    @Attribute("auto_sequence")
    fun getAutoSequence(): GenericAttributeValue<String>

    @SubTagList("odoo")
    fun getOdoos(): List<OdooElement>

    @SubTagList("data")
    fun getDatas(): List<DataElement>

    @SubTagList("openerp")
    fun getOpenerps(): List<OpenerpElement>

    @SubTagsList(value=["odoo", "openerp", "data"])
    fun getOdooOpenerpDatas(): List<OdooOpenerpData>

    @SubTagList("menuitem")
    fun getMenuItems(): List<MenuItem>

    @SubTagList("record")
    fun getRecords(): List<Record>

    @SubTagList("template")
    fun getTemplates(): List<Template>

    @SubTagList("delete")
    fun getDeletes(): List<Delete>

    @SubTagList("act_window")
    fun getActWindows(): List<ActWindow>

    @SubTagList("report")
    fun getReports(): List<Report>

    @SubTagList("function")
    fun getFunctions(): List<Function>

    @SubTagsList(value=["odoo", "openerp", "data", "menuitem", "record", "template", "delete", "act_window", "report", "function"])
    fun getChildren(): List<DomElement>

    // Extra Logic
    fun getRecordsRec(): List<Record>
    fun getTemplatesRec(): List<Template>
}