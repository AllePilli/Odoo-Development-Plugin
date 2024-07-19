package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.indexes.IndexUtil
import com.github.allepilli.odoodevelopmentplugin.throwableComputable
import com.github.allepilli.odoodevelopmentplugin.throwableConsumer
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
 */
data class OdooModelNameIndexItem(val modelNameOffset: Int,
                                  val moduleName: String?,
                                  val parents: List<NameLocation> = emptyList(),
                                  val methods: List<NameLocation> = emptyList()) {
    companion object {
        private fun DataOutput.writeNameLocation(list: List<NameLocation>) =
                DataInputOutputUtil.writeSeq(this, list, throwableConsumer { nameLocation ->
                    IndexUtil.writeString(this, nameLocation.name)
                    DataInputOutputUtil.writeINT(this, nameLocation.offset)
                })

        private fun DataInput.readNameLocation(): List<NameLocation> =
                DataInputOutputUtil.readSeq(this, throwableComputable {
                    NameLocation(IndexUtil.readString(this), DataInputOutputUtil.readINT(this))
                })

        val dataExternalizer = object: DataExternalizer<OdooModelNameIndexItem> {
            override fun save(record: DataOutput, item: OdooModelNameIndexItem) {
                DataInputOutputUtil.writeINT(record, item.modelNameOffset)
                IndexUtil.writeNullableString(record, item.moduleName)

                record.writeNameLocation(item.parents)
                record.writeNameLocation(item.methods)
            }

            override fun read(record: DataInput): OdooModelNameIndexItem = OdooModelNameIndexItem(
                    modelNameOffset = DataInputOutputUtil.readINT(record),
                    moduleName = IndexUtil.readNullableString(record),
                    parents = record.readNameLocation(),
                    methods = record.readNameLocation(),
            )
        }
    }
}