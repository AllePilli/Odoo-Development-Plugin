package com.github.allepilli.odoodevelopmentplugin.core

import com.github.allepilli.odoodevelopmentplugin.extensions.containingModule
import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.FieldInfo
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.NameLocation
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexItem
import com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelNameIndexUtil
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction

/**
 * Represents an Odoo model, which is the result of loading different modules that add/override
 * fields and methods of a model.
 * @param name The model name
 * @param contextModuleName the name of the module, which acts as the context in which the model has been loaded
 */
class Model(project: Project, val name: String, val contextModuleName: String) {
    private val myFields: MutableMap<String, MutableList<SmartPsiElementPointer<*>>> = mutableMapOf()
    private val myMethods: MutableMap<String, MutableList<SmartPsiElementPointer<*>>> = mutableMapOf()
    private val myNames: MutableSet<String> = mutableSetOf()
    private val dependencyGraph = ModuleGraph.createGraph(project, contextModuleName)

    val fields: List<String>
        get() = myFields.keys.toList()

    val allFieldElements: List<SmartPsiElementPointer<*>>
        get() = buildList { myFields.values.forEach(::addAll) }

    val methods: List<String>
        get() = myMethods.keys.toList()

    /**
     * [rawMethodElements] are the Identifiers of the PyFunctions
     */
    val rawMethodElements: List<SmartPsiElementPointer<*>>
        get() = myMethods.values.flatten()

    val methodElements: List<PyFunction>
        get() = rawMethodElements.mapNotNull { it.getPyFunction() }

    init {
        load(project)
    }

    fun getField(name: String) = myFields.getOrDefault(name, mutableListOf())
            .takeIf { it.isNotEmpty() }
    fun getMethod(name: String) = myMethods.getOrDefault(name, mutableListOf())
            .takeIf { it.isNotEmpty() }
    fun getMethodFunction(name: String) = getMethod(name)?.mapNotNull { it.getPyFunction() }

    private fun SmartPsiElementPointer<*>.getPyFunction() = element?.parentOfType<PyFunction>(false)

    fun getInheritedMethods(pyClass: PyClass): List<PyFunction> = methodElements.filter { pyFunction ->
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

                    myFields.getOrPut(field.name) { mutableListOf() }.add(SmartPointerManager.createPointer(element))

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

                    model.fields.forEach { fieldFromDelegate ->
                        model.getField(fieldFromDelegate)?.let { elements ->
                            myFields.getOrPut(fieldFromDelegate) { mutableListOf() }.addAll(elements)
                        }
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