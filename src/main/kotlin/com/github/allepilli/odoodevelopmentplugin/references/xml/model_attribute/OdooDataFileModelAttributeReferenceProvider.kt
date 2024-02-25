package com.github.allepilli.odoodevelopmentplugin.references.xml.model_attribute

import com.github.allepilli.odoodevelopmentplugin.inOdooDataFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

class OdooDataFileModelAttributeReferenceProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(psiElement: PsiElement, context: ProcessingContext): Array<PsiReference> =
            if (psiElement is XmlAttributeValue && psiElement.inOdooDataFile)
                arrayOf(OdooDataFileModelReference(psiElement, TextRange.allOf(psiElement.value).shiftRight(1)))
            else emptyArray()
}