package com.github.allepilli.odoodevelopmentplugin.patterns

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyReferenceExpression

class MethodReferencePattern: PatternCondition<PsiElement>("") {
    companion object {
        fun method() = PlatformPatterns.psiElement().with(MethodReferencePattern())
    }

    override fun accepts(psiElement: PsiElement, context: ProcessingContext?): Boolean {
        if (psiElement.elementType != PyTokenTypes.IDENTIFIER) return false

        val siblings = psiElement.siblings(forward = false, withSelf = false)

        siblings.forEachIndexed { idx, sibling ->
            if (idx == 0) {
                if (sibling.elementType != PyTokenTypes.DOT) return false
            } else if (idx == 1) {
                if (sibling !is PyReferenceExpression) return false

                val child = sibling.firstChild
                if (child.elementType != PyTokenTypes.IDENTIFIER) return false
                return child.text == "self"
            } else return false
        }

        return false
    }
}
