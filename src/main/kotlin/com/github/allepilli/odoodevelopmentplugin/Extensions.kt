package com.github.allepilli.odoodevelopmentplugin

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.util.ProcessingContext

val XmlFile.isOdooDataFile: Boolean
    get() = rootTag?.name in Constants.ODOO_DATA_FILE_ROOTS

val XmlElement.inOdooDataFile: Boolean
    get() = (containingFile as? XmlFile)?.isOdooDataFile == true

fun LighterAST.getChildrenOfType(node: LighterASTNode, type: IElementType): List<LighterASTNode>
    = LightTreeUtil.getChildrenOfType(this, node, type)

fun LighterAST.getChildrenOfType(node: LighterASTNode, tokenSet: TokenSet): List<LighterASTNode> =
        LightTreeUtil.getChildrenOfType(this, node, tokenSet)

inline fun <T, R> Iterable<T>.flatMapNotNull(transform: (T) -> Iterable<R>?): List<R> = mapNotNull(transform).flatten()

fun <T: PsiFile?, Self: PsiFilePattern<T, Self>> PsiFilePattern<T, Self>.withFileName(fileName: String, ) =
        with(object : PatternCondition<T>("withFileName") {
            override fun accepts(file: T & Any, context: ProcessingContext?): Boolean =
                    file.containingFile.name == fileName
        })

val VirtualFile.directories: List<VirtualFile>
    get() {
        if (!isDirectory) throw IllegalStateException("VirtualFile ${this.name} is not a directory")

        return children.filter { it.isDirectory }
    }