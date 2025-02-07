package com.github.allepilli.odoodevelopmentplugin.inspections

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.intellij.util.xml.highlighting.BasicDomElementsInspection

class BasicOdooXmlInspection: BasicDomElementsInspection<OdooOpenerpData>(OdooOpenerpData::class.java)