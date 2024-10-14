package com.github.allepilli.odoodevelopmentplugin.references.python.manifest_module_dependency

import com.github.allepilli.odoodevelopmentplugin.withFileName
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.python.PyElementTypes

private val pattern = PlatformPatterns.psiElement(PyElementTypes.STRING_LITERAL_EXPRESSION)
                .inFile(PlatformPatterns.psiFile().withFileName("__manifest__.py"))
                .withParent(
                        PlatformPatterns.psiElement(PyElementTypes.LIST_LITERAL_EXPRESSION)
                                .withParent(
                                        PlatformPatterns.psiElement(PyElementTypes.KEY_VALUE_EXPRESSION)
                                                .withChild(PlatformPatterns.psiElement(PyElementTypes.STRING_LITERAL_EXPRESSION).withText("'depends'"))
                                )
                )

class ManifestModuleDependencyContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) =
            registrar.registerReferenceProvider(pattern, ManifestModuleDependencyReferenceProvider())
}