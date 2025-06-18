package com.github.allepilli.odoodevelopmentplugin.core

import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.FieldInfo
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.NameLocation
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexItem
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyTargetExpression

/**
 * Represents an Odoo model, which is the result of loading different modules that add/override
 * fields and methods of a model.
 * @param name The model name
 * @param contextModuleName the name of the module, which acts as the context in which the model has been loaded
 */
class Model(project: Project, val name: String, val contextModuleName: String) {
    private val myFields: MutableMap<String, MutableList<Pair<SmartPsiElementPointer<*>, Field>>> = mutableMapOf()
    private val myMethods: MutableMap<String, MutableList<SmartPsiElementPointer<*>>> = mutableMapOf()
    private val myNames: MutableSet<String> = mutableSetOf()
    private val dependencyGraph = ModuleGraph.createGraph(project, contextModuleName)

    val fieldNames: List<String>
        get() = myFields.keys.toList()

    /**
     * Identifiers of the [PyTargetExpression]s
     */
    val rawFields: List<Pair<SmartPsiElementPointer<*>, Field>>
        get() = myFields.values.flatten()

    val fields: List<Pair<PyTargetExpression, Field>>
        get() = rawFields.mapNotNull { (pointer, field) ->
            pointer.getParent<PyTargetExpression>()?.let { targetExpr ->
                targetExpr to field
            }
        }

    val methodNames: List<String>
        get() = myMethods.keys.toList()

    /**
     * Identifiers of the [PyFunction]s
     */
    val rawMethods: List<SmartPsiElementPointer<*>>
        get() = myMethods.values.flatten()

    val methods: List<PyFunction>
        get() = rawMethods.mapNotNull { it.getParent() }

    init {
        load(project)
    }

    private inline fun <reified T: PsiElement> SmartPsiElementPointer<*>.getParent(): T? =
            element?.parentOfType<T>(false)

    /**
     * @return all raw fields in this [Model] with the same [name]
     */
    fun getRawFields(name: String): List<Pair<SmartPsiElementPointer<*>, Field>> = myFields
            .getOrDefault(name, mutableListOf())

    fun getFields(name: String): List<Pair<PyTargetExpression, Field>> = getRawFields(name)
            .mapNotNull { (pointer, field) ->
                pointer.getParent<PyTargetExpression>()?.let { targetExpr ->
                    targetExpr to field
                }
            }

    /**
     * @return all raw methods in this [Model] with the same [name]
     */
    fun getRawMethods(name: String): List<SmartPsiElementPointer<*>> = myMethods.getOrDefault(name, mutableListOf())

    fun getMethods(name: String): List<PyFunction> = getRawMethods(name).mapNotNull { it.getParent() }

    fun getInheritedMethods(pyClass: PyClass): List<PyFunction> = methods.filter { pyFunction ->
        pyClass != pyFunction.containingClass
    }

    /**
     * Loads the model similar to how it gets loaded in odoo,
     * from module 'base' to module '[contextModuleName]'
     */
    private fun load(project: Project) {
        loadNames(project)
        myFields.clear()
        myMethods.clear()

        val loadedModules = mutableSetOf<String>()
        val modulesToLoad = dependencyGraph.nodes.toMutableSet()

        while (modulesToLoad.isNotEmpty()) {
            // calculate the modules that can be loaded next
            val toLoad = modulesToLoad.filter { module ->
                val dependencies = dependencyGraph.getDependencies(module)

                // the empty dependency check only applies to the 'base' module
                dependencies.all { it in loadedModules } || dependencies.isEmpty()
            }.toSet()

            val modelInfos = toLoad.flatMap { moduleName ->
                myNames.map { aName ->
                    aName to OdooModelNameIndexUtil.getModelInfo(project, aName, moduleName)
                }
            }

            val classes = modelInfos.flatMapNotNull { (aName, infos) ->
                infos.mapNotNull { info ->
                    // We assume that one OdooModelNameIndexItem represents only one possible PyClass
                    OdooModelNameIndexUtil.getModels(project, aName, info.moduleName)
                            .firstOrNull { pyClass -> info.represents(pyClass) }
                            ?.let { pyClass -> info to pyClass }
                }
            }

            classes.forEach { (info, pyClass) ->
                val file = pyClass.containingFile
                for (field in info.fields) {
                    val element = file.findElementAt(field.offset) ?: continue

                    myFields.getOrPut(field.name) { mutableListOf() }.add(SmartPointerManager.createPointer(element) to field.toField())

                    // Handle delegated fields (by _inherits or delegate=True)
                    val delegatedModelName = if (field is FieldInfo.Many2OneField && field.delegate) {
                        field.coModelName
                    } else if (field.name in info.delegateMap.keys) {
                        info.delegateMap[field.name]!!
                    } else continue

                    // we need the Model corresponding to the comodel_name of the delegated field in the context
                    // of the class currently being loaded
                    val module = pyClass.containingModule?.name ?: continue
                    val model = Model(project, delegatedModelName, module)

                    model.fieldNames.forEach { fieldFromDelegate ->
                        myFields.getOrPut(fieldFromDelegate) { mutableListOf() }
                                .addAll(model.getRawFields(fieldFromDelegate))
                    }
                }

                for (method in info.methods) {
                    val element = file.findElementAt(method.offset) ?: continue

                    myMethods.getOrPut(method.name) { mutableListOf() }.add(SmartPointerManager.createPointer(element))
                }
            }

            loadedModules.addAll(toLoad)
            modulesToLoad.removeAll(toLoad)
        }
    }

    private fun loadNames(project: Project) {
        myNames.clear()
        myNames.add(name)

        // Traversing the dependency tree up to "base" we find the first module that has one or multiple model info
        // objects.
        val items = dependencyGraph.bfs(contextModuleName)
                .map { moduleName -> OdooModelNameIndexUtil.getModelInfo(project, name, moduleName) }
                .filter(List<OdooModelNameIndexItem>::isNotEmpty)
                .firstOrNull()
                ?: return

        val contextModule = ModuleDependencyIndexUtil.findModuleByName(project, contextModuleName) ?: return
        val checkedParents = mutableSetOf<String>()
        var parents = items.flatMap { item ->  item.parents.map(NameLocation::name) }
                .toSet()

        do {
            val diff = if (checkedParents.isEmpty()) {
                val temp = (parents - checkedParents)
                checkedParents.addAll(parents)
                temp
            } else parents - checkedParents

            parents = diff.flatMap { parentName ->
                OdooModelNameIndexUtil.getModelInfos(project, parentName, contextModule).map { it.parents.map { it.name } }
            }.flatten().toSet()
            checkedParents.addAll(parents)
        } while (parents.isNotEmpty())

        myNames.addAll(checkedParents)
    }
}