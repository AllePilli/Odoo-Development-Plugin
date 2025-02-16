package com.github.allepilli.odoodevelopmentplugin.extensions

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

fun LighterAST.getChildrenOfType(node: LighterASTNode, type: IElementType): List<LighterASTNode>
        = LightTreeUtil.getChildrenOfType(this, node, type)

fun LighterAST.getChildrenOfType(node: LighterASTNode, tokenSet: TokenSet): List<LighterASTNode> =
        LightTreeUtil.getChildrenOfType(this, node, tokenSet)

fun LighterAST.getSingleChild(node: LighterASTNode, type: IElementType) = getChildrenOfType(node, type)
        .singleOrNull()

fun <T> LighterAST.useSingleChild(node: LighterASTNode, type: IElementType, action: (LighterASTNode) -> T) =
        getSingleChild(node, type)?.let(action)

fun LighterAST.firstChild(node: LighterASTNode, type: IElementType, selector: (LighterASTNode) -> Boolean = {true}) =
        getChildrenOfType(node, type).firstOrNull(selector)

fun <T> LighterAST.useFirstChild(node: LighterASTNode, type: IElementType, selector: (LighterASTNode) -> Boolean = {true}, action: (LighterASTNode) -> T) =
        firstChild(node, type, selector)?.let(action)

fun LighterAST.forEachChild(node: LighterASTNode, type: IElementType, action: (LighterASTNode) -> Unit) =
        getChildrenOfType(node, type).forEach(action)

fun <T> LighterAST.mapChildren(node: LighterASTNode, type: IElementType, transform: (LighterASTNode) -> T) =
        getChildrenOfType(node, type).map(transform)

fun <T> LighterAST.mapChildrenNotNull(node: LighterASTNode, type: IElementType, transform: (LighterASTNode) -> T) =
        getChildrenOfType(node, type).mapNotNull(transform)
