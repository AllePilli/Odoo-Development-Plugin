package com.github.allepilli.odoodevelopmentplugin.documentation

import com.github.allepilli.odoodevelopmentplugin.isOdooDataFile
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.PyTokenTypes
import org.toml.lang.psi.ext.elementType

class OdooDomDocumentationTargetProvider: PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? =
            if ((element.containingFile as? XmlFile)?.isOdooDataFile == true) OdooDomDocumentationTarget(element, originalElement)
            else if (
                    (originalElement?.containingFile as? XmlFile)?.isOdooDataFile == true
                    && element.elementType == PyTokenTypes.IDENTIFIER
                    && originalElement is XmlAttributeValue
                    ) {
                // Python identifier reference from odoo data file
                OdooPyFromDomDocumentationTarget(element, originalElement)
            }
            else null
}