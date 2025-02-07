package com.github.allepilli.odoodevelopmentplugin.xml.dom.structure

import com.github.allepilli.odoodevelopmentplugin.isOdooDataFile
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class OdooDomStructureViewBuilderProvider: XmlStructureViewBuilderProvider {
    override fun createStructureViewBuilder(file: XmlFile): StructureViewBuilder? =
            if (DomManager.getDomManager(file.project).getDomFileDescription(file) != null && file.isOdooDataFile)
                OdooDomStructureViewBuilder(file)
            else null
}