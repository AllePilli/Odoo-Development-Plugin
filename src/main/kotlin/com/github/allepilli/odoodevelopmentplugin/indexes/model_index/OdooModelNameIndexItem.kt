package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.indexes.IndexUtil
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.util.ThrowableConsumer
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * @property modelNameOffset The absolute offset of the model name definition in the [com.intellij.openapi.util.TextRange] of the containing file
 */
data class OdooModelNameIndexItem(val modelNameOffset: Int = 0, val parents: List<Pair<String, Int>> = emptyList()) {
    companion object {
        val dataExternalizer = object: DataExternalizer<OdooModelNameIndexItem> {
            override fun save(record: DataOutput, item: OdooModelNameIndexItem) {
                DataInputOutputUtil.writeINT(record, item.modelNameOffset)
                DataInputOutputUtil.writeSeq(record, item.parents, object : ThrowableConsumer<Pair<String, Int>, IOException> {
                    override fun consume(pair: Pair<String, Int>) {
                        IndexUtil.writeString(record, pair.first)
                        DataInputOutputUtil.writeINT(record, pair.second)
                    }
                })
            }

            override fun read(record: DataInput): OdooModelNameIndexItem = OdooModelNameIndexItem(
                    modelNameOffset = DataInputOutputUtil.readINT(record),
                    parents = DataInputOutputUtil.readSeq(record, object: ThrowableComputable<Pair<String, Int>, IOException> {
                        override fun compute(): Pair<String, Int> =
                                IndexUtil.readString(record) to DataInputOutputUtil.readINT(record)
                    }),
            )
        }
    }
}