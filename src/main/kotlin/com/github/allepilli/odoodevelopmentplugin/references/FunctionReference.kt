package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.buildLookupElementWithIcon
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.originalFile
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil
import com.jetbrains.python.psi.types.TypeEvalContext

class FunctionReference(element: PyStringLiteralExpression, rangeInElement: TextRange?) : PsiReferenceBase<PyStringLiteralExpression>(element, rangeInElement), PsiPolyVariantReference {
    override fun resolve(): PsiElement? = multiResolve(false).singleOrNull()?.element
    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> {
        val pyClass = element.parentOfType<PyClass>() ?: return emptyArray()
        val functionName = PyStringLiteralUtil.getStringValue(element.text)

        val modelName = pyClass.getModelName()
        val moduleName = pyClass.containingModule?.name

        if (modelName == null || moduleName == null) {
            // just look in the current class
            return pyClass.findMethodByName(functionName, false, TypeEvalContext.codeAnalysis(element.project, element.containingFile))
                    ?.let { arrayOf(PsiElementResolveResult(it)) }
                    ?: emptyArray()
        }

        val model = Model(pyClass.project, modelName, moduleName)

        return model.getMethodFunction(functionName)
                ?.map(::PsiElementResolveResult)
                ?.toTypedArray()
                ?: emptyArray()
    }

    override fun getVariants(): Array<out Any?> {
        val pyClass = element.parentOfType<PyClass>() ?: return emptyArray()
        val modelName = pyClass.getModelName() ?: return emptyArray()
        val moduleName = element.containingFile.viewProvider.virtualFile
                .originalFile()
                ?.getContainingModule(element.project)
                ?.name
                ?: return emptyArray()
        val model = Model(element.project, modelName, moduleName)

        return model.methodElements
                .map { method ->
                    buildLookupElementWithIcon(method)
                            .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                }
                .toTypedArray()
    }
}