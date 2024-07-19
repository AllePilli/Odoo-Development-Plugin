package com.github.allepilli.odoodevelopmentplugin.extensions

import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression

fun PyClass.hasModelName(modelName: String): Boolean = getModelName() == modelName
fun PyClass.getModelName(): String? {
    findClassAttribute("_name", true, null)?.findAssignedValue()?.let { value ->
        (value as? PyStringLiteralExpression)?.let { expr ->
            return expr.stringValue
        }
    }

    return findClassAttribute("_inherit", true, null)?.findAssignedValue()?.let { value ->
        when (value) {
            is PyStringLiteralExpression -> value.stringValue
            is PyListLiteralExpression -> value.elements
                    .filterIsInstance<PyStringLiteralExpression>()
                    .firstOrNull()
                    ?.stringValue
            else -> null
        }
    }
}