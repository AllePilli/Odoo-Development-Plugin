package com.github.allepilli.odoodevelopmentplugin.references.python.manifest_module_dependency

import com.github.allepilli.odoodevelopmentplugin.references.ModuleReference
import com.github.allepilli.odoodevelopmentplugin.references.python.PyStringLiteralReferenceProvider
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralUtil

class ManifestModuleDependencyReferenceProvider: PyStringLiteralReferenceProvider() {
    override fun getReferences(element: PyStringLiteralExpression, context: ProcessingContext): List<PsiReference> =
            listOf(ModuleReference(element, PyStringLiteralUtil.getContentRange(element.text)))
}