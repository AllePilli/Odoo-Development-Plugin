package com.github.allepilli.odoodevelopmentplugin.dev

import com.github.allepilli.odoodevelopmentplugin.extensions.getPresentableName
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Function
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

class DomViewerNodeDescriptor(project: Project, private val element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<Any>(project, parentDescriptor) {
    init {
        myName = if (element is DomElement) getElementString(element) else element.toString()
    }

    private fun getElementString(element: DomElement): String = when (element) {
        is OdooOpenerpData, is Record, is Template, is ActWindow, is Report, is Function -> element.getPresentableName()
        is MenuItem, is SubMenuItem, is Field, is Value, is Delete -> element.getPresentableName(true)
        else -> "DomElement"
    }

    override fun update(): Boolean = false
    override fun getElement(): Any = element
}