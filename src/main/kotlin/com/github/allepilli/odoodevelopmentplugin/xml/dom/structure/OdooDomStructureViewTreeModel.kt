package com.github.allepilli.odoodevelopmentplugin.xml.dom.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.xml.XmlFileTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Function
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.DomService
import com.intellij.util.xml.structure.DomStructureViewTreeModel

class OdooDomStructureViewTreeModel(file: XmlFile, private val descriptor: Function<DomElement, DomService.StructureViewMode>, editor: Editor?) : DomStructureViewTreeModel(file, descriptor, editor) {
    override fun getRoot(): StructureViewTreeElement {
        val file = psiFile
        val fileElement = DomManager.getDomManager(file.project)
                .getFileElement(file, DomElement::class.java)
                ?: return XmlFileTreeElement(file)

        return OdooDomStructureTreeElement(fileElement.rootElement.createStableCopy(), descriptor, navigationProvider)
    }
}
