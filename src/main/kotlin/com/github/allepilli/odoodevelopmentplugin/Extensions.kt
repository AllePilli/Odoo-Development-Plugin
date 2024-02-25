package com.github.allepilli.odoodevelopmentplugin

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile

val XmlFile.isOdooDataFile: Boolean
    get() = rootTag?.name in Constants.ODOO_DATA_FILE_ROOTS

val XmlElement.inOdooDataFile: Boolean
    get() = (containingFile as? XmlFile)?.isOdooDataFile == true

fun LighterAST.getChildrenOfType(node: LighterASTNode, type: IElementType): List<LighterASTNode>
    = LightTreeUtil.getChildrenOfType(this, node, type)

inline fun <T, R> Iterable<T>.flatMapNotNull(transform: (T) -> Iterable<R>?): List<R> = mapNotNull(transform).flatten()