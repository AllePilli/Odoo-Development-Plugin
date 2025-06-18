package com.github.allepilli.odoodevelopmentplugin.references

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.github.allepilli.odoodevelopmentplugin.core.Field.Relational
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getModelName
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Field
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Record
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.originalFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import com.jetbrains.python.psi.PyClass

class SimpleFieldNameReference(psiElement: PsiElement, rangeInElement: TextRange, private val modelName: String? = null) : PsiPolyVariantReferenceBase<PsiElement>(psiElement, rangeInElement) {
    constructor(attr: XmlAttributeValue): this(attr, TextRange.allOf(attr.value).shiftRight(1))

    /**
     * The co-model name if this [SimpleFieldNameReference] refers to a relational field
     */
    var coModelName: String? = null

    override fun multiResolve(isCompleteCode: Boolean): Array<ResolveResult> = buildArray {
        val project = element.project
        val viewProvider = element.containingFile.viewProvider
        val virtualFile = viewProvider.virtualFile.originalFile() ?: viewProvider.virtualFile
        val currentModule = virtualFile.getContainingModule(project) ?: return@buildArray
        val modelName = modelName ?: calcModelName() ?: return@buildArray
        val fieldName = value
        val model = Model(project, modelName, currentModule.name)
        val fields = model.getFields(fieldName)

        // Here we assume that Field Inheritance cannot change a relational field to a non-relational field and vice versa
        // So it SHOULD (with caution) be safe to just take the first relational field in the list.
        coModelName = fields.firstOrNull { it.second is Relational }
                ?.second
                ?.let { it as Relational }
                ?.coModelName

        val fieldElements = model.getFields(fieldName).map { it.first }

        addAll(PsiElementResolveResult.createResults(fieldElements))
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val virtualFile = element.containingFile.viewProvider.virtualFile.originalFile() ?: return emptyArray()
        val currentModule = virtualFile.getContainingModule(project) ?: return emptyArray()
        val modelName = modelName ?: calcModelName() ?: return emptyArray()
        val model = Model(project, modelName, currentModule.name)

        return model.fieldNames.map { fieldName ->
            LookupElementBuilder.create(fieldName)
                    .withIcon(OdooIcons.odoo)
        }.toTypedArray()
    }

    private fun calcModelName(): String? = if (element is XmlAttributeValue) element.parentOfType<XmlTag>()
            ?.let { fieldTag ->
                DomManager.getDomManager(element.project)
                        .getDomElement(fieldTag)
                        as? Field?
            }
            ?.let { field -> field.parent as? Record? }
            ?.getModel()
            ?.value else element.parentOfType<PyClass>()?.getModelName()
}
