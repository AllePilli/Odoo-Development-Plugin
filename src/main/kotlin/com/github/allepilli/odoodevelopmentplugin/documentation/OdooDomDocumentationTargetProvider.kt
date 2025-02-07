package com.github.allepilli.odoodevelopmentplugin.documentation

import com.github.allepilli.odoodevelopmentplugin.isOdooDataFile
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile

class OdooDomDocumentationTargetProvider: PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? =
            if ((element.containingFile as? XmlFile)?.isOdooDataFile == true) OdooDomDocumentationTarget(element, originalElement)
            else null
}