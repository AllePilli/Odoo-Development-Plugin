package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.indexes.IndexUtil
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput

data class NameLocation(val name: String, val offset: Int)

/**
 * @property modelNameOffset The absolute offset of the model name definition in the [com.intellij.openapi.util.TextRange] of the containing file
 * @property moduleName The module this model is part of
 * @property parents The declared parents of the model, defined by the `_inherit` field
 * @property methods The methods declared in this model
 * @property fields the fields declared in this model
 * @property delegateMap map of field names to comodel names, defined in the '_inherits' field
 */
data class OdooModelNameIndexItem(val modelNameOffset: Int,
                                  val moduleName: String?,
                                  val parents: List<NameLocation> = emptyList(),
                                  val methods: List<NameLocation> = emptyList(),
                                  val fields: List<FieldInfo> = emptyList(),
                                  val delegateMap: Map<String, String> = emptyMap(),
        ) {
    companion object {
        private fun DataOutput.writeNameLocation(list: List<NameLocation>) =
                DataInputOutputUtil.writeSeq(this, list) { nameLocation ->
                    IndexUtil.writeString(this, nameLocation.name)
                    DataInputOutputUtil.writeINT(this, nameLocation.offset)
                }

        private fun DataInput.readNameLocation(): List<NameLocation> =
                DataInputOutputUtil.readSeq(this) {
                    NameLocation(IndexUtil.readString(this), DataInputOutputUtil.readINT(this))
                }

        private fun DataOutput.writeFieldInfo(list: List<FieldInfo>) =
                DataInputOutputUtil.writeSeq(this, list) { fieldInfo ->
                    IndexUtil.writeString(this, fieldInfo.name)
                    DataInputOutputUtil.writeINT(this, fieldInfo.offset)
                    IndexUtil.writeString(this, fieldInfo.type)

                    when (fieldInfo) {
                        is FieldInfo.Many2OneField -> {
                            IndexUtil.writeString(this, fieldInfo.coModelName)
                            IndexUtil.writeBoolean(this, fieldInfo.delegate)
                        }
                        is FieldInfo.AnyField -> {}
                    }
                }

        private fun DataInput.readFieldInfo(): List<FieldInfo> = DataInputOutputUtil.readSeq(this) {
            val name = IndexUtil.readString(this)
            val offset = DataInputOutputUtil.readINT(this)
            val type = IndexUtil.readString(this)

            when (type) {
                FieldInfo.Many2OneField.TYPE -> {
                    val coModelName = IndexUtil.readString(this)
                    val delegate = IndexUtil.readBoolean(this)
                    FieldInfo.Many2OneField(name, offset, coModelName, delegate)
                }
                else -> FieldInfo.AnyField(name, offset)
            }
        }

        private fun DataOutput.writeDelegateMap(map: Map<String, String>) =
                DataInputOutputUtil.writeSeq(this, map.entries) { (fieldName, coModelName) ->
                    IndexUtil.writeString(this, fieldName)
                    IndexUtil.writeString(this, coModelName)
                }

        private fun DataInput.readDelegateMap() = DataInputOutputUtil.readSeq(this) {
            IndexUtil.readString(this) to IndexUtil.readString(this)
        }.toMap()

        val dataExternalizer = object: DataExternalizer<OdooModelNameIndexItem> {
            override fun save(record: DataOutput, item: OdooModelNameIndexItem) {
                DataInputOutputUtil.writeINT(record, item.modelNameOffset)
                IndexUtil.writeNullableString(record, item.moduleName)

                record.writeNameLocation(item.parents)
                record.writeNameLocation(item.methods)
                record.writeFieldInfo(item.fields)
                record.writeDelegateMap(item.delegateMap)
            }

            override fun read(record: DataInput): OdooModelNameIndexItem = OdooModelNameIndexItem(
                    modelNameOffset = DataInputOutputUtil.readINT(record),
                    moduleName = IndexUtil.readNullableString(record),
                    parents = record.readNameLocation(),
                    methods = record.readNameLocation(),
                    fields = record.readFieldInfo(),
                    delegateMap = record.readDelegateMap(),
            )
        }
    }
}