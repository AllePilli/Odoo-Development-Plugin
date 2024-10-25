package com.github.allepilli.odoodevelopmentplugin.inspections.other_quickfixes

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.insertElement
import com.github.allepilli.odoodevelopmentplugin.inspections.OdooLocalQuickFix
import com.github.allepilli.odoodevelopmentplugin.services.OdooModuleChooser
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.ast.findChildByTypeNotNull
import com.jetbrains.python.ast.findChildrenByType
import com.jetbrains.python.psi.*

/**
 * Quick fix that adds a dependency to the [currentModule]
 *
 * When this quick fix is applied the user gets prompted to choose a module from the [modules] list.
 * Once a module is chosen it is added to the manifest of the [currentModule].
 * @property currentModule current working module, chosen dependency will be added in this modules manifest file
 * @property modules list of available modules
 * @see OdooModuleChooser
 */
class AddModuleDependencyQuickFix(val currentModule: VirtualFile, val modules: List<String>): OdooLocalQuickFix {
    companion object {
        val LOG = logger<AddModuleDependencyQuickFix>()
    }

    override val nameKey: String
        get() = "QFIX.add.module.dependency"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (modules.size == 1) {
            addSelectedModuleDependency(project, modules.first())
        } else service<OdooModuleChooser>().selectModule(modules).onSuccess { selectedModuleName ->
            addSelectedModuleDependency(project, selectedModuleName)
        }
    }

    /**
     * Add the [selectedModuleName] to the [currentModule]
     */
    private fun addSelectedModuleDependency(project: Project, selectedModuleName: String) {
        val manifestFile = currentModule.findFile(Constants.MANIFEST_FILE_WITH_EXT)
        if (manifestFile == null) {
            LOG.error("Could not find manifest file of module $selectedModuleName")
            return
        }

        val manifestPsi = PsiManager.getInstance(project).findFile(manifestFile) as? PyFile ?: return
        val exprStatement = manifestPsi.children.firstOrNull { it is PyExpressionStatement }
        if (exprStatement == null) {
            createAndAddManifestDictionary(manifestPsi, selectedModuleName)
            return
        }

        val dict = exprStatement
                .findChildByTypeNotNull<PyDictLiteralExpression>(PyElementTypes.DICT_LITERAL_EXPRESSION)

        val dependsKeyValue = dict.findChildrenByType<PyKeyValueExpression>(PyElementTypes.KEY_VALUE_EXPRESSION)
                .find { keyValExpr ->
                    "depends" == (keyValExpr.key as? PyStringLiteralExpression)
                            ?.text
                            ?.let { PyStringLiteralUtil.getStringValue(it) }
                }

        if (dependsKeyValue == null) {
            createAndAddDependsKeyValue(manifestPsi, dict, selectedModuleName)
        } else {
            try {
                createAndAddDependency(manifestPsi, dependsKeyValue, selectedModuleName)
            } catch (e: IllegalStateException) {
                LOG.warn("${manifestFile.path}: ${e.message}")
            }
        }
    }

    /**
     * Adds a complete dictionary to the [file]
     *
     * A dict literal is created with the 'depends' key containing the [moduleName] as a dependency and adds it to [file]
     * @param file manifest file in which the dict will be added
     * @param moduleName dependency added to the 'depends' list
     */
    private fun createAndAddManifestDictionary(file: PyFile, moduleName: String) {
        val dict = PyElementGenerator.getInstance(file.project)
                .createExpressionFromText(LanguageLevel.forElement(file), """{'depends': ['$moduleName']}""")

        WriteCommandAction.writeCommandAction(file.project)
                .withName("Add manifest dictionary")
                .run<Throwable> {
                    file.add(dict)
                }
    }

    /**
     * Add depends keyword with the new dependency
     *
     * The depends key containing the [moduleName] to the given [dict]
     * @param file manifest file containing [dict]
     * @param dict dictionary to add the depends key value expression to
     * @param moduleName dependency added to the depends list
     */
    private fun createAndAddDependsKeyValue(file: PyFile, dict: PyDictLiteralExpression, moduleName: String) {
        val tempDict = PyElementGenerator.getInstance(dict.project)
                .createExpressionFromText(LanguageLevel.forElement(dict), """{'depends': ['$moduleName']}""")
                as PyDictLiteralExpression

        WriteCommandAction.writeCommandAction(dict.project, file)
                .withName("Add depends key")
                .run<Throwable> {
                    dict.insertElement(tempDict.elements.first())
                }
    }

    /**
     * Add the new dependency to the depends value list
     *
     * Adds a new dependency to the value list of the depends key in the manifest file
     * @throws IllegalStateException when the value of [dependsExpression] is not a [PyListLiteralExpression]
     * @param file manifest file
     * @param dependsExpression key value expression representing the depends key of the manifest file
     * @param moduleName dependency added to the depends list
     */
    private fun createAndAddDependency(file: PyFile, dependsExpression: PyKeyValueExpression, moduleName: String) {
        val dependencyList = dependsExpression.value as? PyListLiteralExpression
                ?: throw IllegalStateException("Depends key in manifest file does not have a literal list as value")

        val dependency = PyElementGenerator.getInstance(file.project)
                .createStringLiteralAlreadyEscaped("'$moduleName'")

        WriteCommandAction.writeCommandAction(file.project, file)
                .withName("Add module dependency")
                .run<Throwable> {
                    dependencyList.insertElement(dependency)
                }
    }
}
