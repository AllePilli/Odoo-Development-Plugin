package com.github.allepilli.odoodevelopmentplugin.completion.model_method_completion

import com.github.allepilli.odoodevelopmentplugin.buildLookupElementWithIcon
import com.github.allepilli.odoodevelopmentplugin.completion.BasicCompletionContributor
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.extensions.uniqueBy
import com.github.allepilli.odoodevelopmentplugin.patterns.MethodReferencePattern
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.jetbrains.python.codeInsight.completion.getPyClass

class ModelMethodCompletionContributor: BasicCompletionContributor<PsiElement>(MethodReferencePattern.method()) {
    override fun getCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet): List<LookupElement> {
        val pyClass = parameters.getPyClass() ?: return emptyList()
        val project = pyClass.project
        val modelName = pyClass.getModelName() ?: return emptyList()
        val module = parameters.originalPosition!!.containingFile.virtualFile.getContainingModule(project) ?: return emptyList()
        val moduleName = module.name
        val model = Model(project, modelName, moduleName)

        return model.methods
                .uniqueBy { it.name }
                .map {
                    buildLookupElementWithIcon(it) {
                        val functionClass = it.containingClass

                        if (functionClass != null) {
                            functionClass.name?.let { className ->
                                val functionModuleName = functionClass.containingModule?.name

                                typeText(if (functionModuleName != null) "$functionModuleName.$className" else className)
                            }
                        }
                    }
                }
    }
}