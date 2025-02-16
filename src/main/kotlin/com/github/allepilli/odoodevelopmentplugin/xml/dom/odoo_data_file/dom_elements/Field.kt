package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters.*
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.named_enums.TypeFieldValueEnum
import com.intellij.util.xml.*

interface Field: DomElement {
    @Required
    @Referencing(SimpleFieldReferenceConverter::class)
    @Attribute("name")
    fun getName(): GenericAttributeValue<String>
}

interface TypeField: Field {
    @Required
    @Attribute("type")
    fun getType(): GenericAttributeValue<TypeFieldValueEnum>
}

interface FileTypeField: TypeField {
    @Required
    @Attribute("file")
    fun getFile(): GenericAttributeValue<String>
}

interface TextTypeField: TypeField {
    @Required
    @TagValue
    fun getText(): String
}

interface IntTypeField: TypeField {
    @Convert(IntConverter::class)
    @TagValue
    fun getData(): Int
}

interface FloatTypeField: TypeField {
    @Required
    @Convert(FloatConverter::class)
    @TagValue
    fun getData(): Float
}

interface CollectionTypeField: Field {
    @Required
    @SubTagList("value")
    fun getValues(): List<Value>
}

interface MarkupTypeField: TypeField {
    // Extra Logic
    fun getCode(): String
}

interface ViewArchField: TypeField
interface ListViewArchField: ViewArchField {
    @SubTag("list")
    fun getList(): ListView
}

interface RefField: Field {
    @Required
    @Convert(RecordReferenceConverter::class)
    @Attribute("ref")
    fun getRef(): GenericAttributeValue<Record>
}

interface EvalField: Field {
    @Required
    @Attribute("eval")
    fun getExpression(): GenericAttributeValue<String>

    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>
}

interface SearchField: Field {
    @Required
    @Attribute("search")
    fun getSearch(): GenericAttributeValue<String>

    @Referencing(ModelReferenceConverter::class)
    @Attribute("model")
    fun getModel(): GenericAttributeValue<String>

    @Attribute("use")
    fun getUse(): GenericAttributeValue<String>
}

interface TextField: Field {
    @Required
    @TagValue
    fun getText(): String
}

interface RecordField: Field {
    @SubTagList("record")
    fun getRecords(): List<Record>
}