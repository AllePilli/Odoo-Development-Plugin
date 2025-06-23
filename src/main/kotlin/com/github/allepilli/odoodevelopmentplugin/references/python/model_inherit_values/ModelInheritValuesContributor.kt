package com.github.allepilli.odoodevelopmentplugin.references.python.model_inherit_values

import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*

class ModelInheritValuesContributor: OdooReferenceContributor(::ModelInheritValuesReferenceProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = StandardPatterns.or(
                psiElement<PyStringLiteralExpression> {
                    parents(PyAssignmentStatement::class, PyStatementList::class, PyClass::class)
                    parent<PyAssignmentStatement> {
                        child<PyTargetExpression> {
                            text = "_inherit"
                        }
                    }
                },
                psiElement<PyStringLiteralExpression> {
                    parents(PyListLiteralExpression::class, PyAssignmentStatement::class, PyStatementList::class, PyClass::class)
                    superParent<PyAssignmentStatement>(2) {
                        child<PyTargetExpression> {
                            text = "_inherit"
                        }
                    }
                }
        )
}