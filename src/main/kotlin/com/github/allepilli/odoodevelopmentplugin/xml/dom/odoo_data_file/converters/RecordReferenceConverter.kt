package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Record

class RecordReferenceConverter: RefReferenceConverter<Record>() {
    override fun getId(any: Record): String? = any.getId().value

    override fun getCandidateElements(root: OdooOpenerpData): List<Record> = root.getRecordsRec()
}