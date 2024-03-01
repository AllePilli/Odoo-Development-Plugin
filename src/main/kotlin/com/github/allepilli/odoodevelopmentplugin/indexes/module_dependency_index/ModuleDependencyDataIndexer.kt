package com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.PsiDependentFileContent
import com.jetbrains.python.psi.PyDictLiteralExpression
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression

class ModuleDependencyDataIndexer: DataIndexer<String, Set<String>, FileContent> {
    override fun map(fileContent: FileContent): MutableMap<String, Set<String>> {
        val psiDependentContent = fileContent as? PsiDependentFileContent ?: return mutableMapOf()
        return with(psiDependentContent.psiFile as? PyFile ?: return mutableMapOf()) {
            mutableMapOf(virtualFile!!.parent!!.name to getModuleDependencies())
        }
    }
}

private fun PyFile.getModuleDependencies(): Set<String> = this@getModuleDependencies.statements.singleOrNull()
        ?.firstChild
        ?.let { it as? PyDictLiteralExpression }
        ?.elements
        ?.firstOrNull { (it.key as? PyStringLiteralExpression)?.stringValue == "depends" }
        ?.value
        ?.let { it as? PyListLiteralExpression }
        ?.elements
        ?.filterIsInstance<PyStringLiteralExpression>()
        ?.map { stringExpr -> stringExpr.stringValue }
        ?.toSet()
        ?: emptySet()