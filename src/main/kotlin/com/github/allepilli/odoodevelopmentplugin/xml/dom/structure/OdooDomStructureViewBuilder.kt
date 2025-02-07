package com.github.allepilli.odoodevelopmentplugin.xml.dom.structure

import com.intellij.ide.structureView.StructureView
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.structureView.newStructureView.StructureViewComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService
import com.intellij.util.Function

class OdooDomStructureViewBuilder(private val file: XmlFile): TreeBasedStructureViewBuilder() {
    companion object {
        val DESCRIPTOR: Function<DomElement, DomService.StructureViewMode> = Function { element ->
            DomService.StructureViewMode.SHOW
        }
    }

    override fun createStructureViewModel(editor: Editor?): StructureViewModel =
            OdooDomStructureViewTreeModel(file, DESCRIPTOR, editor)

    override fun createStructureView(fileEditor: FileEditor?, project: Project): StructureView =
            StructureViewComponent(fileEditor, createStructureViewModel((fileEditor as? TextEditor)?.editor), project, true)
}