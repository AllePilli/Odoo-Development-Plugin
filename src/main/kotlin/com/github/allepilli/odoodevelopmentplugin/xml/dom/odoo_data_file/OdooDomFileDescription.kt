package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Function
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers.*
import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import com.intellij.util.xml.highlighting.DomElementsAnnotator

/**
 * @use https://app.github-file-watcher.com to watch the .rng files in the odoo repository in case the DOM
 * architecture needs to change
 */
class OdooDomFileDescription: DomFileDescription<OdooOpenerpData>(OdooOpenerpData::class.java, "odoo") {
    override fun acceptsOtherRootTagNames(): Boolean = true
    override fun isMyFile(file: XmlFile, module: Module?): Boolean =
            super.isMyFile(file, module) && file.rootTag?.name in setOf("odoo", "data", "openerp")

    override fun createAnnotator(): DomElementsAnnotator = OdooDomElementsAnnotator()
    override fun initializeFileDescription() {
        registerTypeChooser(MenuItem::class.java, MenuItemTypeChooser())
        registerTypeChooser(SubMenuItem::class.java, SubMenuItemTypeChooser())
        registerTypeChooser(NonRootMenuItem::class.java, NonRootMenuItemTypeChooser())
        registerTypeChooser(Field::class.java, FieldTypeChooser())
        registerTypeChooser(Value::class.java, ValueTypeChooser())
        registerTypeChooser(Delete::class.java, DeleteTypeChooser())
        registerTypeChooser(Function::class.java, FunctionTypeChooser())
    }
}