package com.github.allepilli.odoodevelopmentplugin.references.xml.model_attribute

import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

private val pattern = XmlPatterns.xmlAttributeValue(
        XmlPatterns.xmlAttribute("model")
)

class OdooDataFileModelAttributeReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) =
            registrar.registerReferenceProvider(pattern, OdooDataFileModelAttributeReferenceProvider())
}