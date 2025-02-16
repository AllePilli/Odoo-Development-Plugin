package com.github.allepilli.odoodevelopmentplugin.references.python.fields_in_constrains_wrapper

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression

private val pattern = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).withParent(
        PlatformPatterns.psiElement(PyArgumentList::class.java).withParent(
                PlatformPatterns.psiElement(PyCallExpression::class.java).withChild(
                        PlatformPatterns.psiElement(PyReferenceExpression::class.java)
                                .withText("api.constrains")
                ).withParent(
                        PlatformPatterns.psiElement(PyDecorator::class.java)
                )
        )
)

class FieldsInConstrainsWrapperContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar
            .registerReferenceProvider(pattern, FieldsInConstrainsWrapperReferenceProvider())
}