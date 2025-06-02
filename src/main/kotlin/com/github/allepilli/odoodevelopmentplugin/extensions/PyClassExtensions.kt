package com.github.allepilli.odoodevelopmentplugin.extensions

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.jetbrains.python.extensions.inherits
import com.jetbrains.python.nameResolver.FQNamesProvider
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.types.TypeEvalContext

fun PyClass.hasModelName(modelName: String): Boolean = getModelName() == modelName
fun PyClass.getModelName(): String? {
    // TODO: I think this function can be replaced by OdooModelNameIndex.getModelName(PyClass, VirtualFile?)
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

fun PyClass.isOdooBaseModel(): Boolean = qualifiedName in Constants.ODOO_BASE_MODELS_FQN

fun PyClass.isOdooModel(includeBaseModels: Boolean = true): Boolean {
    if (!includeBaseModels && qualifiedName in Constants.ODOO_BASE_MODELS_FQN) return false

    val evalContext = TypeEvalContext.codeAnalysis(project, containingFile)
    val fqnNamesProvider = object : FQNamesProvider {
        override fun getNames(): Array<String> = arrayOf("odoo.models.BaseModel")

        override fun isClass(): Boolean = true
    }

    return inherits(evalContext, fqnNamesProvider)
}