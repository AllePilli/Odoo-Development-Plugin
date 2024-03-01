package com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInput
import java.io.DataOutput

class ModuleDependencyIndexValueExternalizer: DataExternalizer<Set<String>> {
    override fun save(out: DataOutput, set: Set<String>) {
        DataInputOutputUtil.writeINT(out, set.size)
        set.forEach { saveString(out, it) }
    }

    private fun saveString(out: DataOutput, string: String) {
        DataInputOutputUtil.writeINT(out, string.length)
        string.toByteArray().forEach { b ->
            DataInputOutputUtil.writeINT(out, b.toInt())
        }
    }

    override fun read(input: DataInput): Set<String> {
        val size = DataInputOutputUtil.readINT(input)
        return if (size == 0) emptySet() else buildSet {
            for (i in 0 until size) add(readString(input))
        }
    }

    private fun readString(input: DataInput): String {
        val length = DataInputOutputUtil.readINT(input)

        val bytes = if (length == 0) emptyList() else buildList {
            for (i in 0 until length) add(DataInputOutputUtil.readINT(input).toByte())
        }

        return bytes.toTypedArray().toByteArray().toString(Charsets.UTF_8)
    }
}