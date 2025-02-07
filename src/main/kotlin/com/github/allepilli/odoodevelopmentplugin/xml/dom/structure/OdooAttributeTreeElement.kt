package com.github.allepilli.odoodevelopmentplugin.xml.dom.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.xml.XmlAttribute
import javax.swing.Icon

class OdooAttributeTreeElement(val attribute: XmlAttribute) : StructureViewTreeElement, ItemPresentation {
    override fun getValue(): Any = attribute
    override fun getPresentation(): ItemPresentation = this
    override fun getIcon(open: Boolean): Icon? = null
    override fun getChildren(): Array<TreeElement> = arrayOf()

    override fun getPresentableText(): String = if (!attribute.isValid) "<unknown>"
        else try {
            "${attribute.name}: ${attribute.value}"
        } catch (e: IndexNotReadyException) {
            "Name not available during indexing"
        }
}
