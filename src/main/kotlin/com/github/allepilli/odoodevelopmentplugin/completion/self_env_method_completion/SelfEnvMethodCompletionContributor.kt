package com.github.allepilli.odoodevelopmentplugin.completion.self_env_method_completion

import com.github.allepilli.odoodevelopmentplugin.completion.BasicCompletionContributor
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.patterns.SelfEnvMethodReferencePattern
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.vfs.originalFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil
import com.jetbrains.python.psi.PySubscriptionExpression

class SelfEnvMethodCompletionContributor: BasicCompletionContributor<PsiElement>(SelfEnvMethodReferencePattern.method()) {
    override fun getCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet): List<LookupElement> {
        val parent = parameters.position.parent
        val subscriptionExpr = parent.childrenOfType<PySubscriptionExpression>().singleOrNull()
                ?: return emptyList()

        val modelNameExpr = subscriptionExpr.childrenOfType<PyStringLiteralExpression>()
                .singleOrNull()
                ?: return emptyList()
        val modelName = PyStringLiteralUtil.getStringValue(modelNameExpr.text)
        val project = subscriptionExpr.project
        val contextModule = subscriptionExpr.containingFile.viewProvider.virtualFile
                .originalFile()
                ?.getContainingModule(project)
                ?: return emptyList()
        val model = Model(project, modelName, contextModule.name)

        return model.methods.map {
            val parameterNames = it.parameterList.parameters.mapNotNull { param -> param.name }
            var lookupElement = LookupElementBuilder.createWithIcon(it)
                    .withTypeText(it.containingClass?.name ?: "")

            if (parameterNames.isNotEmpty())
                lookupElement = lookupElement.withTailText(parameterNames.joinToString(separator = ", ", prefix = "(", postfix = ")"), true)

            lookupElement
        }
    }
}