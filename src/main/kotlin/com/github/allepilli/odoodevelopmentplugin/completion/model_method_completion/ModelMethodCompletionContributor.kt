package com.github.allepilli.odoodevelopmentplugin.completion.model_method_completion

import com.github.allepilli.odoodevelopmentplugin.InheritanceUtils
import com.github.allepilli.odoodevelopmentplugin.completion.BasicCompletionContributor
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.extensions.uniqueBy
import com.github.allepilli.odoodevelopmentplugin.patterns.MethodReferencePattern
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.jetbrains.python.codeInsight.completion.getPyClass

class ModelMethodCompletionContributor: BasicCompletionContributor(MethodReferencePattern.method()) {
    override fun getCompletions(parameters: CompletionParameters, context: ProcessingContext): List<LookupElement> {
        val pyClass = parameters.getPyClass() ?: return emptyList()
        val modelName = pyClass.getModelName() ?: return emptyList()
        val module = parameters.originalPosition!!.containingFile.virtualFile.getContainingModule(pyClass.project) ?: return emptyList()
        val methods = pyClass.methods.mapNotNull { it.name }.toSet()

        return InheritanceUtils.getAllInheritedMethods(pyClass.project, modelName, module)
                .filter { it.name !in methods } // filter out methods that are already overridden in the class
                .uniqueBy { it.name }
                .map(LookupElementBuilder::createWithIcon)
    }
}