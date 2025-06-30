package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.github.allepilli.odoodevelopmentplugin.references.ModelMethodReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.asSafely
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.GenericDomValue

class ModelMethodReferenceConverter: CustomReferenceConverter<String> {
    override fun createReferences(genericDomValue: GenericDomValue<String>?, element: PsiElement?, context: ConvertContext?): Array<PsiReference> =
            genericDomValue
                    ?.asSafely<GenericAttributeValue<String>>()
                    ?.xmlAttributeValue
                    ?.let { arrayOf(ModelMethodReference(it)) }
                    ?: emptyArray()
}