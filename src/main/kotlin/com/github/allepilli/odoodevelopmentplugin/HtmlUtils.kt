package com.github.allepilli.odoodevelopmentplugin

import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.util.xml.DomElement

object HtmlUtils {
    fun createXmlCodeBlock(project: Project, textSupplier: () -> String): HtmlChunk {
        val codeBuilder = StringBuilder()
        HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                codeBuilder,
                project,
                XMLLanguage.INSTANCE,
                textSupplier(),
                true,
                1.0f,
        )

        return HtmlChunk.raw(codeBuilder.toString())
                .code()
                .wrapWith("pre")
    }

    fun createTextRepresentation(element: DomElement): String {
        val attributeList = element.genericInfo.attributeChildrenDescriptions
                .mapNotNull { description ->
                    val genericAttributeValue = description.getDomAttributeValue(element)

                    if (genericAttributeValue.value == null) null
                    else description.name!! to genericAttributeValue.value!!
                }

        var elementChunk = HtmlChunk.Element.tag(element.xmlElementName)
        for ((attrName, attrValue) in attributeList) {
            elementChunk = elementChunk.attr(attrName, attrValue.toString())
        }

        return elementChunk.toString()
    }
}