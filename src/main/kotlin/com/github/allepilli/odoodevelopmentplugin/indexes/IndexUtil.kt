package com.github.allepilli.odoodevelopmentplugin.indexes

import com.intellij.openapi.util.ThrowableComputable
import com.intellij.util.ThrowableConsumer
import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

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
    fun readNullableString(record: DataInput): String? = DataInputOutputUtil.readNullable(record, object : ThrowableComputable<String, IOException> {
        override fun compute(): String? = readString(record)
    })

    /**
     * Should be coupled with [IndexUtil.readNullableString]
     * @throws java.io.IOException
     */
    fun writeNullableString(record: DataOutput, string: String?) = DataInputOutputUtil.writeNullable(record, string, object: ThrowableConsumer<String, IOException> {
        override fun consume(string: String) = writeString(record, string)
    })
}