package com.github.allepilli.odoodevelopmentplugin.dev

import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.OdooOpenerpData
import com.intellij.ide.util.treeView.IndexComparator
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.DimensionService
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.xml.XmlFile
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.xml.DomManager
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class OdooDomViewerDialog(private val project: Project, selectedEditor: Editor): DialogWrapper(project, true, IdeModalityType.MODELESS) {
    private val myDomTreePanel: JPanel = JPanel(BorderLayout())
    private val myDomTree = Tree()
    private lateinit var myStructureTreeModel: StructureTreeModel<DomViewerTreeStructure>
    private lateinit var myTreeStructure: DomViewerTreeStructure
    private val myOriginalPsiFile = PsiDocumentManager.getInstance(project)
            .getPsiFile(selectedEditor.document)
            as XmlFile

    init {
        init()
    }

    override fun init() {
        val element = DomManager.getDomManager(project)
                .getDomElement(myOriginalPsiFile.rootTag)
                as? OdooOpenerpData
                ?: return

        myTreeStructure = DomViewerTreeStructure(project)
        myStructureTreeModel = StructureTreeModel(myTreeStructure, IndexComparator.getInstance(), disposable)
        myTreeStructure.rootDomElement = element
        myStructureTreeModel.invalidateAsync()
        val treeModel = AsyncTreeModel(myStructureTreeModel, disposable)
        myDomTree.model = treeModel

        with(myDomTree) {
            isRootVisible = false
            showsRootHandles = true
            updateUI()
        }

        val renderer = myDomTree.cellRenderer
        myDomTree.setCellRenderer { tree, value, selected, expanded, leaf, row, hasFocus ->
            val c = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
            if (value is DefaultMutableTreeNode) {
                (value.userObject as? DomViewerNodeDescriptor)?.let { descriptor ->
                    val element = descriptor.element
                    if (c is NodeRenderer) {
                        c.toolTipText = element.toString()
                    }
                }
            }
            c
        }

        val scrollPane = ScrollPaneFactory.createScrollPane(myDomTree, true)
        myDomTreePanel.add(scrollPane, BorderLayout.CENTER)

        DimensionService.getInstance().setSize(dimensionServiceKey, JBUI.size(800, 600), project)
        super.init()
    }

    override fun getDimensionServiceKey(): String = "#com.github.allepilli.odoodevelopmentplugin.dev.DomViewerDialog"

    override fun createCenterPanel(): JComponent = myDomTreePanel
}