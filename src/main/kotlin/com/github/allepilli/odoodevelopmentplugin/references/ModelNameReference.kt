package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttributeValue
import com.jetbrains.python.PythonLanguage

class ModelNameReference(psiElement: PsiElement, rangeInElement: TextRange, val canReferenceContainingClass: Boolean = true)
    : PsiPolyVariantReferenceBase<PsiElement>(psiElement, rangeInElement) {
        constructor(xmlAttributeValue: XmlAttributeValue)
                : this(xmlAttributeValue, TextRange.allOf(xmlAttributeValue.value).shiftRight(1))

    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val module = element.containingFile.virtualFile.getContainingModule(element.project)
        val name = value
        val models = OdooModelNameIndexUtil.findModelsByName(element.project, name, moduleRoot = module)

        if (!canReferenceContainingClass && element.language == PythonLanguage.getInstance()) {
            addAll(models
                    .filter { pyClass ->
                        pyClass.containingFile.virtualFile.path != element.containingFile.virtualFile.path
                                || (element.textOffset < pyClass.textOffset || element.textOffset >= pyClass.textOffset + pyClass.textLength)
                    }
                    .map(::PsiElementResolveResult))
        } else {
            addAll(models.map(::PsiElementResolveResult))
        }
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val modelNames = OdooModelNameIndexUtil.getAllModelNames(project)

        return modelNames.map {
            LookupElementBuilder.create(it)
                    .withIcon(OdooIcons.odoo)
        }.toTypedArray()
    }
}