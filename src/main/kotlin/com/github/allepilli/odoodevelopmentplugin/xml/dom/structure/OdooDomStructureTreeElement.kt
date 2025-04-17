package com.github.allepilli.odoodevelopmentplugin.xml.dom.structure

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.buildArray
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.Function
import com.intellij.util.xml.*
import com.intellij.util.xml.DomService.StructureViewMode.SHOW
import com.intellij.util.xml.DomService.StructureViewMode.SHOW_CHILDREN
import javax.swing.Icon

class OdooDomStructureTreeElement(
        val element: DomElement,
        private val descriptor: Function<DomElement, DomService.StructureViewMode>,
        private val navigationProvider: DomElementNavigationProvider?) : StructureViewTreeElement, ItemPresentation {

    override fun getValue(): Any? = if (element.isValid) element.xmlElement else null
    override fun getPresentation(): ItemPresentation = this
    override fun navigate(requestFocus: Boolean) { navigationProvider?.navigate(element, requestFocus) }
    override fun canNavigate(): Boolean = navigationProvider != null && navigationProvider.canNavigate(element)
    override fun canNavigateToSource(): Boolean = canNavigate()
    override fun getIcon(open: Boolean): Icon? = OdooIcons.odoo
    override fun getPresentableText(): String? = if (!element.isValid) "<unknown>"
    else try {
        val presentation = element.presentation
        presentation.elementName ?: presentation.typeName
    } catch (e: IndexNotReadyException) {
        "Name not available during indexing"
    }

    override fun getChildren(): Array<TreeElement> = buildArray {
        if (!element.isValid) return@buildArray

        element.genericInfo.attributeChildrenDescriptions
                .map { description -> description.getDomAttributeValue(element) }
                .mapNotNull { it.xmlAttribute }
                .forEach {
                    add(createAttributeElement(it))
                }

        DomUtil.acceptAvailableChildren(element, object: DomElementVisitor {
            override fun visitDomElement(element: DomElement) {
                if (element is GenericDomValue<*>) return

                when (descriptor.`fun`(element)) {
                    SHOW -> add(createChildElement(element))
                    SHOW_CHILDREN -> DomUtil.acceptAvailableChildren(element, this)
                    else -> {}
                }
            }
        })
    }

    private fun createChildElement(element: DomElement): StructureViewTreeElement =
            OdooDomStructureTreeElement(element, descriptor, navigationProvider)

    private fun createAttributeElement(element: XmlAttribute): StructureViewTreeElement =
            OdooAttributeTreeElement(element)
}
