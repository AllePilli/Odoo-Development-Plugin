package com.github.allepilli.odoodevelopmentplugin.python

import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.patterns.SelfEnvMethodReferencePattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.ast.findChildrenByType
import com.jetbrains.python.codeInsight.PyCustomMember
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.PyClassMembersProviderBase
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext

class ModelMethodClassMemberProvider: PyClassMembersProviderBase() {
    override fun getMembers(clazz: PyClassType?, location: PsiElement?, context: TypeEvalContext): MutableCollection<PyCustomMember> {
        if (clazz?.classQName == "odoo.models.BaseModel" && location is PySubscriptionExpression) {
            return getSelfEnvMethods(location, context)
        }

        val pyClass = when {
            location is PyReferenceExpression && location.text == "self" -> clazz?.pyClass
            location is PyCallExpression && location.text == "super()" -> location.parentOfType<PyClass>()
            else -> null
        } ?: return mutableListOf()

        val modelName = pyClass.getModelName() ?: return mutableListOf()
        val project = pyClass.project
        val contextModule = context.origin?.virtualFile?.getContainingModule(project) ?: return mutableListOf()
        val model = Model(project, modelName, contextModule.name)

        return model.getInheritedMethods(pyClass)
                .map { method -> PyCustomMember(method.name!!, method) }
                .toMutableList()
    }

    private fun getSelfEnvMethods(location: PySubscriptionExpression, context: TypeEvalContext): MutableList<PyCustomMember> {
        // check for self.env['model.name']
        if (!SelfEnvMethodReferencePattern.isSelfEnvPattern(location)) return mutableListOf()

        val modelName = location.findChildrenByType<PsiElement>(PyElementTypes.STRING_LITERAL_EXPRESSION)
                .let { (modelLitStr) -> PyStringLiteralUtil.getStringValue(modelLitStr.text) }

        val project = location.project
        val contextModule = context.origin?.virtualFile?.getContainingModule(project) ?: return mutableListOf()
        val model = Model(project, modelName, contextModule.name)

        return model.methodElements
                .map { PyCustomMember(it.name!!, it) }
                .toMutableList()
    }
}