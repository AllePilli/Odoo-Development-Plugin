package com.github.allepilli.odoodevelopmentplugin.inspections.redundant_module_dependency

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.github.allepilli.odoodevelopmentplugin.inspections.OdooPyInspection
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.jetbrains.python.psi.*

class RedundantModuleDependencyInspection: OdooPyInspection() {
    private val quickFix = RemoveRedundantModuleDependencyQuickFix()

    override fun isAvailableForFile(file: PsiFile): Boolean = file.name == Constants.MANIFEST_FILE_WITH_EXT

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : PyElementVisitor() {
                override fun visitPyKeyValueExpression(node: PyKeyValueExpression) {
                    super.visitPyKeyValueExpression(node)

                    // Only highlight elements of the 'depends' key
                    if ((node.key as? PyStringLiteralExpression)?.text?.let(PyStringLiteralUtil::getStringValue) != "depends")
                        return

                    val dependencyListNode = node.value as? PyListLiteralExpression ?: return
                    val dependencies = dependencyListNode.elements
                            .filterIsInstance<PyStringLiteralExpression>()
                            .map {
                                val name = PyStringLiteralUtil.getStringValue(it.text)
                                val dependencyScope = ModuleDependencyIndexUtil.findAllDependencies(it.project, name)

                                Triple(name, dependencyScope, it)
                            }

                    for ((name, _, depNode) in dependencies) {
                        val (otherName, _, _) = dependencies.find { (_, othersDependencies, _) ->
                            name in othersDependencies
                        } ?: continue

                        // Identified redundant dependency (name)
                        holder.registerProblem(depNode,
                                StringsBundle.message("INSP.redundant.module.dependency.module.is.part.of.others.dependencies", name, otherName),
                                quickFix)
                    }
                }
            }
}