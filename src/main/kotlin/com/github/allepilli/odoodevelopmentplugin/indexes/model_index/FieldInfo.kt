package com.github.allepilli.odoodevelopmentplugin.indexes.model_index


sealed class FieldInfo(val name: String, val offset: Int, val type: String = EMPTY_TYPE) {
    companion object {
        const val EMPTY_TYPE = "ET"
    }

    class Many2OneField(name: String, offset: Int, val coModelName: String, val delegate: Boolean): FieldInfo(name, offset, TYPE) {
        companion object {
            const val TYPE = "Many2One"
        }

        override fun toString(): String = "Many2OneField(name='$name', offset=$offset, coModelName='$coModelName', delegate=$delegate)"
        override fun equals(other: Any?): Boolean = super.equals(other)
                && (other is Many2OneField)
                && delegate == other.delegate
                && coModelName == other.coModelName

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + coModelName.hashCode()
            result = 31 * result + delegate.hashCode()
            return result
        }
    }

    class AnyField(name: String, offset: Int): FieldInfo(name, offset, EMPTY_TYPE) {
        override fun toString(): String = "AnyField(name='$name', offset=$offset)"
    }

    override fun toString(): String = "FieldInfo(name='$name', offset=$offset)"
    override fun equals(other: Any?): Boolean = (other is FieldInfo)
            && name == other.name
            && offset == other.offset

    override fun hashCode(): Int {
        var result = offset
        result = 31 * result + name.hashCode()
        return result
    }
}
