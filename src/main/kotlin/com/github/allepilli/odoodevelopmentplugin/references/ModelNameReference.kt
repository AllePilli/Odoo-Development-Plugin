package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.Utils
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

class ModelNameReference(psiElement: PsiElement, rangeInElement: TextRange)
    : PsiReferenceBase<PsiElement>(psiElement, rangeInElement), PsiPolyVariantReference {
    override fun resolve(): PsiElement? = multiResolve(false).singleOrNull()?.element
    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val module = Utils.getContainingModule(element.containingFile)
        val name = buildString {
            append(element.text, rangeInElement.startOffset, rangeInElement.endOffset)
        }
        addAll(OdooModelNameIndexUtil.findModelsByName(element.project, name, moduleRoot = module)
                .map(::PsiElementResolveResult))
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