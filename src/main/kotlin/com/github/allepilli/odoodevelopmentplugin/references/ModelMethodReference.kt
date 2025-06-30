package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.buildLookupElementWithIcon
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Function
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.types.TypeEvalContext


class ModelMethodReference(psiElement: PsiElement, rangeInElement: TextRange?): PsiPolyVariantReferenceBase<PsiElement>(psiElement, rangeInElement) {
    constructor(xmlAttributeValue: XmlAttributeValue) : this(xmlAttributeValue, TextRange.allOf(xmlAttributeValue.value).shiftRight(1))

    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val functionName = value
        val (modelName, moduleName) = getModelAndModuleNames()

        if (modelName == null || moduleName == null) {
            if (element.language == PythonLanguage.getInstance()) {
                // look in the current class
                element.parentOfType<PyClass>()
                        ?.findMethodByName(functionName, false, TypeEvalContext.codeAnalysis(element.project, element.containingFile))
                        ?.let { add(PsiElementResolveResult(it)) }
            }

            return@buildArray
        }

        val model = Model(element.project, modelName, moduleName)
        addAll(model.getMethods(functionName).map(::PsiElementResolveResult))
    }

    private fun getModelAndModuleNames(): Pair<String?, String?> {
        return if (element is XmlAttributeValue) {
            val domManager = DomManager.getDomManager(element.project)
            val function = element.parentOfType<XmlTag>(withSelf = false)
                    ?.let(domManager::getDomElement)
                    ?.let { it as? Function }
                    ?: return null to null

            function.getModel().value to element.containingModule?.name
        } else {
            val pyClass = element.parentOfType<PyClass>()
            pyClass?.getModelName() to pyClass?.containingModule?.name
        }
    }

    override fun getVariants(): Array<out Any?> {
        val (modelName, moduleName) = getModelAndModuleNames()
        if (modelName == null || moduleName == null) return emptyArray()

        return Model(element.project, modelName, moduleName)
                .methods
                .map { method ->
                    buildLookupElementWithIcon(method)
                            .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                }
                .toTypedArray()
    }
}