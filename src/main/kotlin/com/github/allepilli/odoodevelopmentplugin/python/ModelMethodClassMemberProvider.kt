package com.github.allepilli.odoodevelopmentplugin.python

import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.codeInsight.PyCustomMember
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.types.PyClassMembersProviderBase
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext

class ModelMethodClassMemberProvider: PyClassMembersProviderBase() {
    override fun getMembers(clazz: PyClassType?, location: PsiElement?, context: TypeEvalContext): MutableCollection<PyCustomMember> {
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
}