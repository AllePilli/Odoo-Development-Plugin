package com.github.allepilli.odoodevelopmentplugin.references.python.manifest_module_dependency

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.patterns.dsl.psiElement
import com.github.allepilli.odoodevelopmentplugin.references.python.OdooReferenceContributor
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyKeyValueExpression
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression

class ManifestModuleDependencyContributor: OdooReferenceContributor(::ManifestModuleDependencyReferenceProvider) {
    override val pattern: ElementPattern<out PsiElement>
        get() = psiElement<PyStringLiteralExpression> {
            file<PyFile> {
                name = Constants.MANIFEST_FILE_WITH_EXT
            }

            parent<PyListLiteralExpression> {
                parent<PyKeyValueExpression> {
                    child<PyStringLiteralExpression> {
                        text = "'depends'"
                    }
                }
            }
        }
}