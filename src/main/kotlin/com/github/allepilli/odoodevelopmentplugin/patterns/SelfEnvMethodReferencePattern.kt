package com.github.allepilli.odoodevelopmentplugin.patterns

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.ast.findChildrenByType
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.StringLiteralExpression
import org.toml.lang.psi.ext.elementType

class SelfEnvMethodReferencePattern: PatternCondition<PsiElement>("") {
    companion object {
        fun method() = PlatformPatterns.psiElement().with(SelfEnvMethodReferencePattern())

        fun isSelfEnvPattern(element: PySubscriptionExpression): Boolean {
            val callsMatches = null != element.childrenOfType<PyReferenceExpression>()
                    .takeIf {
                        true == it.singleOrNull()
                                ?.findChildrenByType<PsiElement>(PyTokenTypes.IDENTIFIER)
                                ?.let { identifier -> identifier.singleOrNull()?.text == "env" }
                    }
                    ?.let { (envExpr) ->
                        null != envExpr.childrenOfType<PyReferenceExpression>()
                                .takeIf {
                                    true == it.singleOrNull()
                                            ?.findChildrenByType<PsiElement>(PyTokenTypes.IDENTIFIER)
                                            ?.let { identifier -> identifier.singleOrNull()?.text == "self" }
                                }
                    }

            if (!callsMatches) return false
            val t = element.childrenOfType<StringLiteralExpression>().size == 1

            return t
        }
    }

    override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        if (element.elementType != PyTokenTypes.IDENTIFIER) return false

        return isCallChainEndingInSelfEnv(element)
    }

    private fun isCallChainEndingInSelfEnv(element: PsiElement): Boolean {
        element.siblings(forward = false, withSelf = false).forEachIndexed { idx, sibling ->
            when (idx) {
                0 -> if (sibling.elementType != PyTokenTypes.DOT) return false
                1 -> when (sibling) {
                    is PySubscriptionExpression -> return isSelfEnvPattern(sibling)
                    is PyCallExpression -> {
                        val refExpr = sibling.firstChild as? PyReferenceExpression ?: return false
                        val identifier = refExpr.lastChild
                                .takeIf { it.elementType == PyTokenTypes.IDENTIFIER }
                                ?: return false
                        return isCallChainEndingInSelfEnv(identifier)
                    }
                    else -> return false
                }
                else -> return false
            }
        }

        return false
    }
}