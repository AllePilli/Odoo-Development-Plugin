package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.util.xml.DomElement
import com.intellij.util.xml.GenericAttributeValue

fun DomElement.getPresentableName(withType: Boolean = false): String = if (withType) {
    val typeName = getSimpleClassName()
    "${xmlTag?.name}: $typeName${getAttributesList()}"
} else {
    "${xmlTag?.name}: ${getAttributesList()}"
}

fun DomElement.getAttributesList(): String =
        genericInfo.attributeChildrenDescriptions
                .map { description -> description.getDomAttributeValue(this) }
                .joinToAttributeList()

fun List<GenericAttributeValue<*>>.joinToAttributeList(): String =
        filter { it.xmlAttribute != null && it.value != null }
                .joinToString(prefix = "(", separator = ", ", postfix = ")") { genericAttributeValue ->
                    "${genericAttributeValue.xmlAttribute?.name}='${genericAttributeValue.value}'"
                }

fun DomElement.getSimpleClassName(default: String = "DomElement"): String =
        this::class.simpleName
                ?.split("$$", limit = 2)
                ?.firstOrNull()
                ?: default
