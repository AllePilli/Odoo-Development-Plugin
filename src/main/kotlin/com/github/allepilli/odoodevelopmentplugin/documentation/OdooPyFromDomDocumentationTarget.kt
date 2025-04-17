package com.github.allepilli.odoodevelopmentplugin.documentation

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.github.allepilli.odoodevelopmentplugin.extensions.getContainingModule
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.Field
import com.intellij.model.Pointer
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.util.text.buildHtml
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyAssignmentStatement

private val EMPTY_PRESENTATION = TargetPresentation.builder("Odoo Data File")
        .icon(OdooIcons.odoo)
        .presentation()

class OdooPyFromDomDocumentationTarget(val element: PsiElement, private val originalElement: PsiElement): DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        val originalElementPtr = originalElement.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            val originalElement = originalElementPtr.dereference() ?: return@Pointer null
            OdooDomDocumentationTarget(element, originalElement)
        }
    }

    override fun computePresentation(): TargetPresentation {
        val currentTag = originalElement.findParentOfType<XmlTag>() ?: return EMPTY_PRESENTATION
        val domManager = DomManager.getDomManager(element.project)
        val currentDomElement = domManager.getDomElement(currentTag) ?: return EMPTY_PRESENTATION

        return when (currentDomElement) {
            is Field -> TargetPresentation.builder("Odoo Field")
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
        val text = element.parentOfType<PyAssignmentStatement>(false)
                ?.text
                ?: return null
        val file = element.containingFile?.viewProvider?.virtualFile
        val moduleName = file?.getContainingModule(element.project)?.name

        val code = buildString {
            HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                    this,
                    element.project,
                    PythonLanguage.INSTANCE,
                    text,
                    false,
                    1f,
            )
        }

        return buildHtml {
            appendRaw(code)

            if (file != null) append(HtmlChunk.fragment(
                    HtmlChunk.hr(),
                    HtmlChunk.tag("icon").attr("src", "AllIcons.Language.Python"),
                    HtmlChunk.nbsp(),
                    HtmlChunk.text(file.name),
            ))

            if (moduleName != null) append(HtmlChunk.fragment(
                    HtmlChunk.hr(),
                    HtmlChunk.tag("icon").attr("src", "AllIcons.Nodes.Folder"),
                    HtmlChunk.nbsp(),
                    HtmlChunk.text(moduleName),
            ))
        }
    }
}