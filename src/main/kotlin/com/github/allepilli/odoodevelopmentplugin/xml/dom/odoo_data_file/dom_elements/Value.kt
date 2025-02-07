package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.ModelReferenceConverter
import com.intellij.util.xml.*

interface Value: DomElement {
    @Attribute("name")
    fun getName(): GenericAttributeValue<String>

    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>

    @Attribute("use")
    fun getUse(): GenericAttributeValue<String>
}

interface SearchValue: Value {
    @Required
    @Attribute("search")
    fun getSearch(): GenericAttributeValue<String>
}

interface EvalValue: Value {
    @Required
    @Attribute("eval")
    fun getEval(): GenericAttributeValue<String>
}

interface TypeValue: Value {
    @Attribute("type")
    fun getType(): GenericAttributeValue<String>
}

interface FileTypeValue: TypeValue {
    @Required
    @Attribute("file")
    fun getFile(): GenericAttributeValue<String>
}

interface TextTypeValue: TypeValue {
    @Required
    @TagValue
    fun getText(): String
}