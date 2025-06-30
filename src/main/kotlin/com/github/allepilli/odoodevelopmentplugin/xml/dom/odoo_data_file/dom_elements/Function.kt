package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelMethodReferenceConverter
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.intellij.util.xml.*

interface Function: Env {
    @Required
    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>

    @Required
    @Referencing(ModelMethodReferenceConverter::class)
    @Attribute("name")
    fun getName(): GenericAttributeValue<String>
}

interface EvalFunction: Function {
    @Required
    @Attribute("eval")
    fun getEval(): GenericAttributeValue<String>
}

interface TreeFunction: Function {
    @SubTagList("value")
    fun getValues(): List<Value>

    @SubTagList("function")
    fun getFunctions(): List<Function>

    @SubTagsList(value=["value", "function"])
    fun getChildren(): List<DomElement>
}