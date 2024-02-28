package com.github.allepilli.odoodevelopmentplugin.references.python.model_inherit_values

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.*

private val inheritAssStmtPattern = PlatformPatterns.psiElement(PyAssignmentStatement::class.java)
        .withChild(
                PlatformPatterns.psiElement(PyTargetExpression::class.java)
                        .withText("_inherit")
        )

private val stringValuePattern = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
        .withParents(PyAssignmentStatement::class.java, PyStatementList::class.java, PyClass::class.java)
        .withParent(inheritAssStmtPattern)

private val stringListValuePattern = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
        .withParents(PyListLiteralExpression::class.java, PyAssignmentStatement::class.java, PyStatementList::class.java, PyClass::class.java)
        .withSuperParent(2, inheritAssStmtPattern)

private val pattern = StandardPatterns.or(stringValuePattern, stringListValuePattern)

class ModelInheritValuesContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar
            .registerReferenceProvider(pattern, ModelInheritValuesReferenceProvider())
}