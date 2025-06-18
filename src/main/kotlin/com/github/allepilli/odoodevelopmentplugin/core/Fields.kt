package com.github.allepilli.odoodevelopmentplugin.core

import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.FieldInfo

sealed class Field(open val name: String, open val offset: Int) {
    abstract class Relational(name: String, offset: Int, open val coModelName: String): Field(name, offset)

    data class Many2One(
            override val name: String,
            override val offset: Int,
            override val coModelName: String,
            val delegate: Boolean,
    ): Relational(name, offset, coModelName)

    data class One2Many(
            override val name: String,
            override val offset: Int,
            override val coModelName: String,
    ): Relational(name, offset, coModelName)

    data class Many2Many(
            override val name: String,
            override val offset: Int,
            override val coModelName: String,
    ): Relational(name, offset, coModelName)

    data class AnyField(
            override val name: String,
            override val offset: Int,
    ): Field(name, offset)
}

fun FieldInfo.toField(): Field = when (this) {
    is FieldInfo.Many2OneField -> Field.Many2One(name, offset, coModelName, delegate)
    is FieldInfo.One2ManyField -> Field.One2Many(name, offset, coModelName)
    is FieldInfo.Many2ManyField -> Field.Many2Many(name, offset, coModelName)
    is FieldInfo.AnyField -> Field.AnyField(name, offset)
    else -> throw IllegalStateException("Unknown field info type: ${this::class}")
}