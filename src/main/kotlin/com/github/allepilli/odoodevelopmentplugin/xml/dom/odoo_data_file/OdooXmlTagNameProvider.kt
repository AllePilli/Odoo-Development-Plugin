package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.reflect.DomChildrenDescription
import com.intellij.xml.XmlTagNameProvider

class OdooXmlTagNameProvider: XmlTagNameProvider {
    override fun addTagNameVariants(elements: MutableList<LookupElement?>?, tag: XmlTag, prefix: String?) {
        DomManager.getDomManager(tag.project)
                .getFileElement(tag.containingFile as? XmlFile, OdooOpenerpData::class.java)
                ?.rootElement
                ?: return

        if (elements == null) return

        val domManager = DomManager.getDomManager(tag.project)
        val parent = domManager.getDomElement(tag.parent as? XmlTag) ?: return
        val genericInfo = parent.genericInfo

        val lookupStrings = buildSet {
            addAll(genericInfo.collectionChildrenDescriptions.getSearchApplicableStrings(prefix))
            addAll(genericInfo.fixedChildrenDescriptions.getSearchApplicableStrings(prefix))
            addAll(domManager.getExtraLookupTags(parent, prefix))
            addAll(elements.mapNotNull { it?.lookupString })
        }

        val newElements = lookupStrings.map { LookupElementBuilder.create(it).withIcon(AllIcons.Nodes.Tag) }
        elements.clear()
        elements.addAll(newElements)
    }

    private fun DomManager.getExtraLookupTags(tag: DomElement, prefix: String?): List<String> = buildList {
        when (tag) {
            is TextField -> {
                val mock = createMockElement(RecordField::class.java, null, false)
                val info = getGenericInfo(mock.domElementType)

                addAll(info.collectionChildrenDescriptions.getSearchApplicableStrings(prefix))
                addAll(info.fixedChildrenDescriptions.getSearchApplicableStrings(prefix))
            }
            is ViewArchField -> {
                val mock = createMockElement(ListViewArchField::class.java, null, false)
                val info = getGenericInfo(mock.domElementType)

                addAll(info.fixedChildrenDescriptions.getSearchApplicableStrings(prefix))
            }
        }
    }

    private fun List<DomChildrenDescription>.getSearchApplicableStrings(prefix: String?): List<String> =
            filter { prefix == null || it.name.startsWith(prefix) }
                    .map { it.name }
}