package com.github.allepilli.odoodevelopmentplugin.dev

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Function
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.util.ObjectUtils
import com.intellij.util.xml.DomElement

class DomViewerTreeStructure(private val project: Project): AbstractTreeStructure() {
    private val myRootElement = ObjectUtils.sentinel("Dom Viewer Root")

    private var myShowTreeNodes = true

    var rootDomElement: DomElement? = null

    override fun getRootElement(): Any = myRootElement

    override fun getChildElements(element: Any): Array<Any> {
        if (myRootElement == element) {
            return if (rootDomElement == null) emptyArray() else arrayOf(rootDomElement!!)
        }

        return ReadAction.compute<Array<Any>, Throwable> {
            if (myShowTreeNodes) buildList {
                when (element) {
                    is OdooOpenerpData -> addChildren(element)
                    is MenuItem -> addChildren(element)
                    is SubMenuItem -> addChildren(element)
                    is Record -> addChildren(element)
                    is Field -> addChildren(element)
                    is Value -> addChildren(element)
                    is Template -> addChildren(element)
                    is Delete -> addChildren(element)
                    is ActWindow -> addChildren(element)
                    is Report -> addChildren(element)
                    is Function -> addChildren(element)
                }
            }.toTypedArray() else emptyArray()
        }
    }

    override fun getParentElement(element: Any): Any? {
        if (element == myRootElement) return null
        if (element == rootDomElement) rootDomElement
        return null
    }

    override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> =
            if (element == myRootElement) object: NodeDescriptor<Any>(project, null) {
                override fun update(): Boolean = false
                override fun getElement(): Any = myRootElement
            } else DomViewerNodeDescriptor(project, element, parentDescriptor)

    override fun commit() {}
    override fun hasSomethingToCommit(): Boolean = false
}

private fun MutableList<DomElement>.addChildren(element: OdooOpenerpData) {
    addAll(element.getChildren())
}

private fun MutableList<DomElement>.addChildren(element: MenuItem) {
    if (element is RootMenuItem) {
        addAll(element.getSubMenuItems())
    } else if (element is TreeNonRootMenuItem) {
        addAll(element.getSubMenuItems())
    }
}

private fun MutableList<DomElement>.addChildren(element: SubMenuItem) {
    if (element is TreeSubMenuItem) addAll(element.getSubMenuItems())
}

private fun MutableList<DomElement>.addChildren(element: Record) {
    addAll(element.getFields())
}

private fun MutableList<DomElement>.addChildren(field: Field) {
    if (field is CollectionTypeField) {
        addAll(field.getValues())
    } else if (field is RecordField) {
        addAll(field.getRecords())
    }
}

private fun MutableList<DomElement>.addChildren(value: Value) {

}

private fun MutableList<DomElement>.addChildren(template: Template) {

}

private fun MutableList<DomElement>.addChildren(delete: Delete) {

}

private fun MutableList<DomElement>.addChildren(actWindow: ActWindow) {

}

private fun MutableList<DomElement>.addChildren(report: Report) {

}

private fun MutableList<DomElement>.addChildren(function: Function) {
    if (function is TreeFunction) addAll(function.getChildren())
}
