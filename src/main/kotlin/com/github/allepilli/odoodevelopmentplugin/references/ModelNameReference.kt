package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

class ModelNameReference(psiElement: PsiElement, rangeInElement: TextRange)
    : PsiReferenceBase<PsiElement>(psiElement, rangeInElement), PsiPolyVariantReference {
    override fun resolve(): PsiElement? = multiResolve(false).singleOrNull()?.element
    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val module = element.containingFile.getContainingModule()
        val name = buildString {
            append(element.text, rangeInElement.startOffset, rangeInElement.endOffset)
        }
        addAll(OdooModelIndexUtil.findModelsByName(element.project, name, moduleRoot = module)
                .map(::PsiElementResolveResult))
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val modelNames = OdooModelIndexUtil.getAllModelNames(project)

        return modelNames.map {
            LookupElementBuilder.create(it)
                    .withIcon(OdooIcons.odoo)
        }.toTypedArray()
    }
}