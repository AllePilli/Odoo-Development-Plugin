package com.github.allepilli.odoodevelopmentplugin.references.python

import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar

abstract class OdooReferenceContributor(private val providerProducer: () -> PsiReferenceProvider): PsiReferenceContributor() {
    abstract val pattern: ElementPattern<out PsiElement>

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern, providerProducer())
    }
}