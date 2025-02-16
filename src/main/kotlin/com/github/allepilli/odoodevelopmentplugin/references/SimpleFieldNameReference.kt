package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.extensions.getText
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Field
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Record
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.originalFile
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import com.jetbrains.python.psi.PyClass

class SimpleFieldNameReference(psiElement: PsiElement, rangeInElement: TextRange) : PsiReferenceBase<PsiElement>(psiElement, rangeInElement), PsiPolyVariantReference {
    constructor(attr: XmlAttributeValue): this(attr, TextRange.allOf(attr.value).shiftRight(1))

    override fun resolve(): PsiElement? = multiResolve(false).singleOrNull()?.element
    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val project = element.project
        val currentModule = element.containingFile.virtualFile.getContainingModule(project) ?: return@buildArray
        val modelName = getModelName() ?: return@buildArray
        val fieldName = element.getText(rangeInElement) ?: return@buildArray
        val model = Model(project, modelName, currentModule.name)

        val fieldElements = model.getField(fieldName)
                ?.mapNotNull { it.element }
                ?: return@buildArray

        addAll(fieldElements.map(::PsiElementResolveResult))
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val virtualFile = element.containingFile.viewProvider.virtualFile.originalFile() ?: return emptyArray()
        val currentModule = virtualFile.getContainingModule(project) ?: return emptyArray()
        val modelName = getModelName() ?: return emptyArray()
        val model = Model(project, modelName, currentModule.name)

        return model.fields.map { fieldName ->
            LookupElementBuilder.create(fieldName)
                    .withIcon(OdooIcons.odoo)
        }.toTypedArray()
    }

    private fun getModelName(): String? {
        return if (element is XmlAttributeValue) {
            val fieldTag = element.parentOfType<XmlTag>() ?: return null
            val field = DomManager.getDomManager(element.project)
                    .getDomElement(fieldTag)
                    as? Field
                    ?: return null

            val record = field.parent as? Record ?: return null

            record.getModel().value
        } else {
            val pyClass = element.parentOfType<PyClass>() ?: return null

            pyClass.getModelName() ?: return null
        }
    }
}
