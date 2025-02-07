package com.github.allepilli.odoodevelopmentplugin.completion

import com.github.allepilli.odoodevelopmentplugin.isOdooDataFile
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.*
import com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.dom_elements.*
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.util.ProcessingContext
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.Required
import com.intellij.util.xml.reflect.DomAttributeChildDescription
import kotlin.reflect.KClass

private val pattern = PlatformPatterns.psiElement(XmlToken::class.java)
    .withParent(XmlPatterns.xmlAttribute())

class OdooXmlAttributeCompletionContributor: BasicCompletionContributor<XmlToken>(pattern) {
    override fun getCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet): List<LookupElement> {
        val isOdooDataFile = (parameters.position.containingFile as? XmlFile)?.isOdooDataFile ?: return emptyList()
        if (!isOdooDataFile) return emptyList()

        val tag = parameters.position.parent.parent as? XmlTag ?: return emptyList()
        val domManager = DomManager.getDomManager(parameters.position.project)
        val tagElement = domManager.getDomElement(tag) ?: return emptyList()
        val takenAttributes = tag.attributes.map { it.name }.toSet()

        val choiceElementAttributes = getChoiceElementAttribute(tagElement, domManager)
        val tagElementAttributes = tagElement.genericInfo.attributeChildrenDescriptions
        val elements = mutableListOf<LookupElementBuilder>()
        val addedCompletionNames = mutableSetOf<String>()

        for (attrDescription in tagElementAttributes) {
            if (attrDescription.name !in takenAttributes && attrDescription.name !in addedCompletionNames) {
                var lookupElement = LookupElementBuilder.create(attrDescription.name)
                        .withInsertHandler(XmlAttributeInsertHandler())

                if (attrDescription.getAnnotation(Required::class.java) != null)
                    lookupElement = lookupElement.withTypeText("required", true)

                addedCompletionNames.add(attrDescription.name)
                elements.add(lookupElement)
            }
        }

        for (attrDescription in choiceElementAttributes) {
            if (attrDescription.name !in takenAttributes && attrDescription.name !in addedCompletionNames) {
                val lookupElement = LookupElementBuilder.create(attrDescription.name)
                        .withInsertHandler(XmlAttributeInsertHandler())

                addedCompletionNames.add(attrDescription.name)
                elements.add(lookupElement)
            }
        }

        if (elements.isNotEmpty()) {
            resultSet.stopHere()
        }

        return elements
    }

    /**
     * This function handles attribute completion for types that are used in our [TypeChooser]'s, because they are too
     * complex for the dom api to figure out. Essentially this method will return all the possible
     * attribute names for every class the [TypeChooser] handles
     */
    private fun getChoiceElementAttribute(element: DomElement, domManager: DomManager): List<DomAttributeChildDescription<*>> {
        when (element) {
            is RootMenuItem -> if (element.getAction().value == null && element.getWebIcon().value == null) {
                return domManager.getAttributeChildDescriptions(NonRootMenuItem::class)
            }
            is TreeSubMenuItem -> if (element.getSubMenuItems().isEmpty()) {
                return domManager.getAttributeChildDescriptions(ActionSubMenuItem::class)
            }
            is TreeNonRootMenuItem -> if (element.getSubMenuItems().isEmpty()) {
                return domManager.getAttributeChildDescriptions(ActionNonRootMenuItem::class)
            }
            is TextTypeField -> return domManager.getAttributeChildDescriptions(
                    FileTypeField::class,
                    IntTypeField::class,
                    FloatTypeField::class,
                    CollectionTypeField::class
            )
            is TextField -> return domManager.getAttributeChildDescriptions(
                    TypeField::class,
                    RefField::class,
                    EvalField::class,
                    SearchField::class
            )
            is TextTypeValue ->
                return domManager.getAttributeChildDescriptions(SearchValue::class, EvalValue::class, FileTypeValue::class)
            is IdDelete -> {} // so we don't fall into the 'is Delete' branch of the when
            is SearchDelete -> {}
            is Delete -> return domManager.getAttributeChildDescriptions(IdDelete::class, SearchDelete::class)
            is TreeFunction -> if (element.getChildren().isEmpty()) return domManager.getAttributeChildDescriptions(EvalFunction::class)
        }

        return emptyList()
    }

    private fun <T: DomElement> DomManager.mock(kClass: KClass<T>) = createMockElement(kClass.java, null, false)

    private fun <T: DomElement> DomManager.getAttributeChildDescriptions(vararg classes: KClass<out T>): List<DomAttributeChildDescription<*>> {
        return classes
                .map {
                    mock(it).genericInfo.attributeChildrenDescriptions
                }
                .fold(listOf()) { acc, descriptions ->
                    acc + descriptions
                }
    }
}