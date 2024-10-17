package com.github.allepilli.odoodevelopmentplugin.inspections.redundant_module_dependency

import com.github.allepilli.odoodevelopmentplugin.inspections.OdooLocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyListLiteralExpression

class RemoveRedundantModuleDependencyQuickFix: OdooLocalQuickFix {
    override val nameKey: String
        get() = "QFIX.remove.redundant.module.dependency"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val stringExpr = descriptor.psiElement
        val listExpr = stringExpr.parent as PyListLiteralExpression
        val elements = listExpr.elements
        val toDelete = mutableListOf(stringExpr)

        if (elements.size > 1) {
            // Remove ',' before or after the string
            val index = elements.indexOfFirst { it.text == stringExpr.text }
            stringExpr.siblings(forward = index != elements.size - 1, withSelf = false)
                    .firstOrNull { it.elementType == PyTokenTypes.COMMA }
                    ?.let(toDelete::add)
        }

        toDelete.forEach(PsiElement::delete)
    }
}
