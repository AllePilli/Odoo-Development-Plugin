package com.github.allepilli.odoodevelopmentplugin.patterns.dsl

import com.github.allepilli.odoodevelopmentplugin.withFileName
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiFile

class PsiFilePatternDsl<T: PsiFile>(var pattern: PsiFilePattern.Capture<T>) {
    var parentDirectoryName: String? = null
    var fileType: FileType? = null
    var name: String? = null

    /**
     * DO NOT CALL
     */
    fun _build(): PsiFilePattern.Capture<T> {
        if (parentDirectoryName != null) pattern = pattern.withParentDirectoryName(
                StandardPatterns.string().withLength(parentDirectoryName!!.length).contains(parentDirectoryName!!)
        )
        if (fileType != null) pattern = pattern.withFileType(StandardPatterns.instanceOf(fileType!!.javaClass))
        if (name != null) pattern = pattern.withFileName(name!!)

        return pattern
    }

    fun parentDirectoryName(init: StringPatternDsl.() -> Unit = {}) {
        val stringPattern = string(init)
        pattern = pattern.withParentDirectoryName(stringPattern)
    }

    inline fun <reified R: T> originalFile(init: PsiFilePatternDsl<R>.() -> Unit = {}) {
        val originalFilePattern = psiFile<R>(init)
        pattern = pattern.withOriginalFile(originalFilePattern)
    }

    fun <V: VirtualFile> virtualFile(vFilePattern: ElementPattern<V>) { pattern = pattern.withVirtualFile(vFilePattern) }
    fun <FT: FileType> fileType(fileTypePattern: ElementPattern<FT>) { pattern = pattern.withFileType(fileTypePattern) }
}

inline fun <reified T: PsiFile> psiFile(init: PsiFilePatternDsl<T>.() -> Unit = {}): PsiFilePattern.Capture<T> {
    val psiFilePatternDsl = PsiFilePatternDsl(PlatformPatterns.psiFile(T::class.java))
    psiFilePatternDsl.init()
    return psiFilePatternDsl._build()
}