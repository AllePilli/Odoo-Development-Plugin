package com.github.allepilli.odoodevelopmentplugin.indexes

import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput

object IndexUtil {
    /**
     * Should be coupled with [IndexUtil.writeString]
     * @throws java.io.IOException
     */
    fun readString(record: DataInput): String = buildString {
        val length = DataInputOutputUtil.readINT(record)
        repeat(length) {
            append(record.readChar())
        }
    }

    /**
     * Should be coupled with [IndexUtil.readString]
     * @throws java.io.IOException
     */
    fun writeString(record: DataOutput, string: String) {
        DataInputOutputUtil.writeINT(record, string.length)
        record.writeChars(string)
    }

    /**
     * Should be coupled with [IndexUtil.writeNullableString]
     * @throws java.io.IOException
     */
    fun readNullableString(record: DataInput): String? = DataInputOutputUtil.readNullable(record) { readString(record) }

    /**
     * Should be coupled with [IndexUtil.readNullableString]
     * @throws java.io.IOException
     */
    fun writeNullableString(record: DataOutput, string: String?) = DataInputOutputUtil.writeNullable(record, string) { str -> writeString(record, str) }

    fun writeBoolean(record: DataOutput, boolean: Boolean) = DataInputOutputUtil.writeINT(record, if (boolean) 1 else 0)
    fun readBoolean(record: DataInput) = DataInputOutputUtil.readINT(record) == 1
}