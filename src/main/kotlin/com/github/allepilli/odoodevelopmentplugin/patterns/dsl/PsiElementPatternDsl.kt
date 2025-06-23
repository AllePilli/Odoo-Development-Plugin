package com.github.allepilli.odoodevelopmentplugin.patterns.dsl

import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ProcessingContext
import kotlin.reflect.KClass

class PsiElementPatternDsl<T: PsiElement>(var pattern: PsiElementPattern.Capture<T>) {
    var text: String? = null
    var elementType: Any? = null
    var reference: KClass<PsiReference>? = null
    var withoutText: String? = null
    var name: String? = null
    var textLength: Int? = null
    var minTextLength: Int? = null
    var language: Language? = null

    /**
     * DO NOT CALL
     */
    fun _build(): PsiElementPattern.Capture<T> {
        text?.let { pattern = pattern.withText(it) }
        @Suppress("UNCHECKED_CAST")
        if (elementType != null) pattern = when (elementType) {
            is IElementType -> pattern.withElementType(elementType as IElementType)
            is TokenSet -> pattern.withElementType(elementType as TokenSet)
            is ElementPattern<*> -> pattern.withElementType(elementType as ElementPattern<IElementType>)
            else -> throw IllegalStateException("elementType should be of one of these types (IElementType, TokenSet, ElementPattern<IElementType>), got ${elementType!!::class}")
        }
        reference?.let { pattern = pattern.withReference(it.java) }
        withoutText?.let { pattern = pattern.withoutText(it) }
        name?.let { pattern = pattern.withName(it) }
        textLength?.let {
            pattern = pattern.with(object: PatternCondition<T>("withTextLength") {
                override fun accepts(t: T, context: ProcessingContext?): Boolean = t.textLength == it
            })
        }
        minTextLength?.let { pattern = pattern.withTextLengthLongerThan(it) }
        language?.let { pattern = pattern.withLanguage(it) }

        return pattern
    }

    fun parents(vararg types: KClass<out PsiElement>) {
        pattern = pattern.withParents(*types.map { it.java }.toTypedArray())
    }

    inline fun <reified ParentType: PsiElement> parent(init: PsiElementPatternDsl<ParentType>.() -> Unit = {}) {
        val parentPattern = psiElement<ParentType>(init)
        pattern = pattern.withParent(parentPattern)
    }

    inline fun <reified SuperParentType: PsiElement> superParent(level: Int, init: PsiElementPatternDsl<SuperParentType>.() -> Unit = {}) {
        val superParentPattern = psiElement<SuperParentType>(init)
        pattern = pattern.withSuperParent(level, pattern)
    }

    inline fun <reified ChildType: PsiElement> child(init: PsiElementPatternDsl<ChildType>.() -> Unit = {}) {
        val childPattern = psiElement<ChildType>(init)
        pattern = pattern.withChild(childPattern)
    }

    inline fun <reified R: PsiElement> firstNonWhitespaceChild(init: PsiElementPatternDsl<R>.() -> Unit = {}) {
        val childPattern = psiElement<R>(init)
        pattern = pattern.withFirstNonWhitespaceChild(childPattern)
    }

    fun whitespace() { pattern = pattern.whitespace() }
    fun whitespaceCommentOrError() { pattern = pattern.whitespaceCommentOrError() }

    inline fun <reified F: PsiFile> file(init: PsiFilePatternDsl<F>.() -> Unit = {}) {
        val psiFilePattern = psiFile<F>(init)
        pattern = pattern.inFile(psiFilePattern)
    }

    fun <V: VirtualFile> virtualFile(vFilePattern: ElementPattern<V>) { pattern = pattern.inVirtualFile(vFilePattern) }
    fun equalTo(other: T) { pattern = pattern.equalTo(other) }

    fun name(vararg names: String) { pattern = pattern.withName(*names) }
    fun name(namePattern: ElementPattern<String>) { pattern = pattern.withName(namePattern) }

    fun notEmpty() { minTextLength = 0 }

    fun text(textPattern: ElementPattern<*>) { pattern = pattern.withText(textPattern) }
    fun withoutText(textPattern: ElementPattern<*>) { pattern = pattern.withoutText(textPattern) }

    fun metaData(metaDataPattern: ElementPattern<PsiMetaData>) { pattern = pattern.withMetaData(metaDataPattern) }

    inline fun <reified R: PsiElement> referencing(init: PsiElementPatternDsl<R>.() -> Unit = {}) {
        val psiElementPattern = psiElement<R>(init)
        pattern = pattern.referencing(psiElementPattern)
    }

    fun compiled() { pattern = pattern.compiled() }

    inline fun <reified ParentType: PsiElement> treeParent(init: PsiElementPatternDsl<ParentType>.() -> Unit = {}) {
        val treeParentPattern = psiElement<ParentType>(init)
        pattern = pattern.withTreeParent(treeParentPattern)
    }

    inline fun <reified ParentType: PsiElement> insideStarting(init: PsiElementPatternDsl<ParentType>.() -> Unit = {}) {
        val parentPattern = psiElement<ParentType>(init)
        pattern = pattern.insideStarting(parentPattern)
    }
}

inline fun <reified T: PsiElement> psiElement(init: PsiElementPatternDsl<T>.() -> Unit = {}): PsiElementPattern.Capture<T> {
    val psiElementPatternDsl = PsiElementPatternDsl(PlatformPatterns.psiElement(T::class.java))
    psiElementPatternDsl.init()
    return psiElementPatternDsl._build()
}
