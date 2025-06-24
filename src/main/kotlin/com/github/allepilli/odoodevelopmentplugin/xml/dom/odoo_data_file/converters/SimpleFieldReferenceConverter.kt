package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.VersionConstants
import com.github.allepilli.odoodevelopmentplugin.references.SimpleFieldNameReference
import com.github.allepilli.odoodevelopmentplugin.services.OdooVersionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.GenericDomValue

class SimpleFieldReferenceConverter: CustomReferenceConverter<String> {
    override fun createReferences(genericDomValue: GenericDomValue<String>?, element: PsiElement?, context: ConvertContext?): Array<PsiReference> {
        if (genericDomValue == null) return emptyArray()

        if (genericDomValue is GenericAttributeValue<String>) {
            val value = genericDomValue.xmlAttributeValue ?: return emptyArray()

            val virtualFile = context?.file?.originalFile?.virtualFile ?: return emptyArray()
            val odooVersionManager = value.project.service<OdooVersionManager>()
            val reference = if (odooVersionManager.versionAtLeast(virtualFile, VersionConstants.L18N_FIELD_NAMES)) {
                // an '@' in the field name indicates that it is a translated field
                val atIdx = value.value.indexOf('@')
                if (atIdx != -1) SimpleFieldNameReference(value, TextRange.create(1, atIdx + 1))
                else SimpleFieldNameReference(value)
            } else SimpleFieldNameReference(value)

            return arrayOf(reference)
        }

        return emptyArray()
    }
}