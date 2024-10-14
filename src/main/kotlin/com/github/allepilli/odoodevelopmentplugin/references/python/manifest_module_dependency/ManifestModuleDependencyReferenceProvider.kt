package com.github.allepilli.odoodevelopmentplugin.references.python.manifest_module_dependency

import com.github.allepilli.odoodevelopmentplugin.references.ModuleReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralUtil

class ManifestModuleDependencyReferenceProvider: PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
            arrayOf(ModuleReference(element, TextRange.allOf(PyStringLiteralUtil.getStringValue(element.text)).shiftRight(1)))
}