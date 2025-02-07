package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.implementations

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Record
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.RecordField
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Template


abstract class OdooOpenerpDataImpl: OdooOpenerpData {
    override fun getRecordsRec(): List<Record> = buildList {
        var currentRoots: List<OdooOpenerpData> = listOf(this@OdooOpenerpDataImpl)
        var currentRecords: List<Record> = currentRoots.flatMap { it.getRecords() }
        var currentFields: List<RecordField>

        do {
            addAll(currentRecords)
            currentRoots = currentRoots.flatMap { it.getOdooOpenerpDatas() }
            currentFields = currentRecords.flatMap { it.getFields() }.filterIsInstance<RecordField>()
            currentRecords = currentFields.flatMap { it.getRecords() } + currentRoots.flatMap { it.getRecords() }
        } while (currentRecords.isNotEmpty())
    }

    override fun getTemplatesRec(): List<Template> = buildList {
        var currentRoots: List<OdooOpenerpData> = listOf(this@OdooOpenerpDataImpl)
        var currentTemplates: List<Template> = currentRoots.flatMap { it.getTemplates() }

        do {
            addAll(currentTemplates)
            currentRoots = currentRoots.flatMap { it.getOdooOpenerpDatas() }
            currentTemplates = currentRoots.flatMap { it.getTemplates() }
        } while (currentTemplates.isNotEmpty())
    }
}