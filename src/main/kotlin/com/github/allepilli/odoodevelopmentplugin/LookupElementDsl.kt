package com.github.allepilli.odoodevelopmentplugin

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import javax.swing.Icon

class LookupElementDsl private constructor(private var builder: LookupElementBuilder) {
    constructor(lookupString: String): this(LookupElementBuilder.create(lookupString))
    constructor(element: PsiNamedElement): this(LookupElementBuilder.createWithIcon(element))

    val lookupElement: LookupElementBuilder
        get() = builder

    fun element(element: PsiElement) {
        builder = builder.withPsiElement(element)
    }

    fun icon(icon: Icon) {
        builder = builder.withIcon(icon)
    }

    fun typeText(text: String) {
        builder = builder.withTypeText(text)
    }

    fun tailText(text: String) {
        builder = builder.withTailText(text)
    }
}

fun buildLookupElement(lookupString: String, init: LookupElementDsl.() -> Unit = {}): LookupElementBuilder =
        LookupElementDsl(lookupString).apply(init).lookupElement

fun buildLookupElementWithIcon(element: PsiNamedElement, init: LookupElementDsl.() -> Unit = {}): LookupElementBuilder =
        LookupElementDsl(element).apply(init).lookupElement