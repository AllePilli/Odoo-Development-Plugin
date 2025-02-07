package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.typechoosers

import com.intellij.psi.xml.XmlTag
import kotlin.reflect.KClass

abstract class AttrDomClassChooser(
        private val defaultClass: KClass<*>,
        private vararg val attrsClasses: Pair<String, KClass<*>>
): DomClassChooser(*(attrsClasses.map { it.second } + listOf(defaultClass)).toTypedArray()) {
    final override fun chooseClass(tag: XmlTag): KClass<*> = attrsClasses
            .firstOrNull { (attrName, _) -> tag.hasAttribute(attrName) }
            ?.second
            ?: defaultClass

    final override fun distinguishTag(tag: XmlTag, klass: KClass<*>) {
        for ((attrName, clazz) in attrsClasses) {
            if (klass == clazz) {
                tag.setAttribute(attrName, "temp_$attrName")
                return
            }
        }
    }
}