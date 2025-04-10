package com.github.allepilli.odoodevelopmentplugin.documentation

import com.github.allepilli.odoodevelopmentplugin.HtmlUtils
import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.core.Model
import com.github.allepilli.odoodevelopmentplugin.extensions.appendXmlCodeBlock
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Field
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Record
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.RefField
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Template
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.util.text.buildHtml
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager

private val EMPTY_PRESENTATION = TargetPresentation.builder("Odoo Data File")
        .icon(OdooIcons.odoo)
        .presentation()

class OdooDomDocumentationTarget(val element: PsiElement, private val originalElement: PsiElement?): DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        val originalElementPtr = originalElement?.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            OdooDomDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override fun computePresentation(): TargetPresentation {
        val currentTag = originalElement?.findParentOfType<XmlTag>() ?: return EMPTY_PRESENTATION
        val domManager = DomManager.getDomManager(element.project)
        val currentDomElement = domManager.getDomElement(currentTag) ?: return EMPTY_PRESENTATION

        return when (currentDomElement) {
            is RefField -> TargetPresentation.builder("Record Reference")
                    .icon(OdooIcons.odoo)
                    .presentation()
            is Template -> TargetPresentation.builder("Template Reference")
                    .icon(OdooIcons.odoo)
                    .presentation()
            else -> EMPTY_PRESENTATION
        }
    }

    override fun computeDocumentationHint(): String? = computeDocumentationString()

    override fun computeDocumentation(): DocumentationResult? {
        val string = computeDocumentationString() ?: return null
        return DocumentationResult.documentation(string)
    }

    private fun computeDocumentationString(): String? {
        val currentTag = originalElement?.findParentOfType<XmlTag>() ?: return null
        val domManager = DomManager.getDomManager(element.project)
        val currentDomElement = domManager.getDomElement(currentTag) ?: return null

        when (currentDomElement) {
            is RefField -> {
                val recordXmlTag = element as? XmlTag ?: return null
                val record = domManager.getDomElement(recordXmlTag) as? Record ?: return null

                return record.computeOdooDocumentationHint("Record")
            }
            is Template -> {
                val templateXmlTag = element as? XmlTag ?: return null
                val template = domManager.getDomElement(templateXmlTag) as? Template ?: return null

                return template.computeOdooDocumentationHint("Template")
            }
            else -> return null
        }
    }
}

private fun DomElement.computeOdooDocumentationHint(default: String): String {
    val project = xmlElement?.project ?: return default
    val file = xmlElement?.containingFile?.viewProvider?.virtualFile

    return buildHtml {
        appendXmlCodeBlock(project) {
            HtmlUtils.createTextRepresentation(this@computeOdooDocumentationHint)
        }

        if (file != null) append(HtmlChunk.fragment(
                HtmlChunk.hr(),
                HtmlChunk.tag("icon").attr("src", "AllIcons.FileTypes.Xml"),
                HtmlChunk.nbsp(),
                HtmlChunk.text(file.name),
        ))
    }
}