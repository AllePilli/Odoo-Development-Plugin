package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.TypeChooser
import java.lang.reflect.Type
import kotlin.reflect.KClass

abstract class DomClassChooser(private vararg val classes: KClass<*>): TypeChooser() {
    private val typeMap: Map<Type, KClass<*>> by lazy {
        buildMap {
            classes.forEach { subClass -> put(subClass.java, subClass) }
        }
    }
    private val javaClasses: Array<Type> by lazy { typeMap.keys.toTypedArray() }

    final override fun getChooserTypes(): Array<Type> = javaClasses
    final override fun chooseType(tag: XmlTag): Type = chooseClass(tag).java
    final override fun distinguishTag(tag: XmlTag, type: Type) {
        val klass = typeMap[type] ?: return
        distinguishTag(tag, klass)
    }

    abstract fun chooseClass(tag: XmlTag): KClass<*>
    abstract fun distinguishTag(tag: XmlTag, klass: KClass<*>)

    protected fun XmlTag.hasAttribute(name: String): Boolean = getAttribute(name) != null
    protected fun XmlTag.hasAttribute(name: String, value: String): Boolean = hasAttribute(name) && getAttributeValue(name) == value
}