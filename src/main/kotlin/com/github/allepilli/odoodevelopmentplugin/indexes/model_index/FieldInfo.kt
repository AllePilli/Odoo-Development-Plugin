package com.github.allepilli.odoodevelopmentplugin.indexes.model_index


sealed class FieldInfo(val name: String, val offset: Int, val type: String = EMPTY_TYPE) {
    companion object {
        const val EMPTY_TYPE = "ET"
    }

    abstract class RelationalField(name: String, offset: Int, val coModelName: String, type: String): FieldInfo(name, offset, type) {
        override fun equals(other: Any?): Boolean = super.equals(other)
                && (other is RelationalField)
                && coModelName == other.coModelName

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + coModelName.hashCode()
            return result
        }
    }

    class Many2OneField(name: String, offset: Int, coModelName: String, val delegate: Boolean): RelationalField(name, offset, coModelName, TYPE) {
        companion object {
            const val TYPE = "Many2One"
        }

        override fun toString(): String = "Many2OneField(name='$name', offset=$offset, coModelName='$coModelName', delegate=$delegate)"
        override fun equals(other: Any?): Boolean = super.equals(other)
                && (other is Many2OneField)
                && delegate == other.delegate

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + delegate.hashCode()
            return result
        }
    }

    class One2ManyField(name: String, offset: Int, coModelName: String): RelationalField(name, offset, coModelName, TYPE) {
        companion object {
            const val TYPE = "One2Many"
        }

        override fun toString(): String = "One2ManyField(name='$name', offset=$offset, coModelName='$coModelName')"
        override fun equals(other: Any?): Boolean = super.equals(other)
                && (other is One2ManyField)
    }

    class Many2ManyField(name: String, offset: Int, coModelName: String): RelationalField(name, offset, coModelName, TYPE) {
        companion object {
            const val TYPE = "Many2Many"
        }

        override fun toString(): String = "Many2ManyField(name='$name', offset=$offset, coModelName='$coModelName')"
        override fun equals(other: Any?): Boolean = super.equals(other)
                && (other is Many2ManyField)
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
