package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.references.SimpleFieldNameReference
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
            return arrayOf(SimpleFieldNameReference(value))
        }

        return emptyArray()
    }
}