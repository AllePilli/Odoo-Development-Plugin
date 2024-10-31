package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.computeReadAction
import com.github.allepilli.odoodevelopmentplugin.extensions.addonPaths
import com.github.allepilli.odoodevelopmentplugin.extensions.findOdooModule
import com.github.allepilli.odoodevelopmentplugin.extensions.getAllFiles
import com.github.allepilli.odoodevelopmentplugin.extensions.hasModelName
import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Processor
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.ValueProcessor
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import java.io.File

class OdooModelNameIndex: FileBasedIndexExtension<String, OdooModelNameIndexItem>() {
    private val indexer = OdooModelNameIndexer()

    override fun getName(): ID<String, OdooModelNameIndexItem> = OdooModelNameIndexUtil.NAME
    override fun dependsOnFileContent(): Boolean = true
    override fun getVersion(): Int = 2
    override fun getIndexer(): DataIndexer<String, OdooModelNameIndexItem, FileContent> = indexer
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<OdooModelNameIndexItem> = OdooModelNameIndexItem.dataExternalizer

    override fun getInputFilter(): FileBasedIndex.InputFilter =
            object: DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE) {
                override fun acceptInput(file: VirtualFile): Boolean {
                    val project = ProjectLocator.getInstance().guessProjectForFile(file)
                    val addonPaths = project?.addonPaths

                    return if (addonPaths?.isNotEmpty() == true) {
                        addonPaths.any { file.path.startsWith(it) } && "${File.separatorChar}tests${File.separatorChar}" !in file.path && !file.nameWithoutExtension.startsWith("__")
                    } else {
                        // Fall back on some hardcoded values
                        val path = file.path.split(File.separatorChar)

                        ("addons" in path || "enterprise" in path) && "tests" !in path && !file.nameWithoutExtension.startsWith("__")
                    }
                }
            }
}

object OdooModelNameIndexUtil {
    val NAME = ID.create<String, OdooModelNameIndexItem>("OdooModelNameIndex")

    fun processAllNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) =
            FileBasedIndex.getInstance().processAllKeys(NAME, processor, scope, filter)

    fun processItems(name: String,
                     processor: ValueProcessor<OdooModelNameIndexItem>,
                     scope: GlobalSearchScope,
                     inFile: VirtualFile? = null,
                     filter: IdFilter? = null) = FileBasedIndex.getInstance().processValues(NAME, name, inFile, processor, scope, filter)

    fun getModuleDependencyScope(project: Project, moduleDirectory: VirtualFile, includeModule: Boolean = true) = if (moduleDirectory.isDirectory) {
        val dependencyFiles = ModuleDependencyIndexUtil.findAllDependencies(project, moduleDirectory.name)
                .flatMapNotNull { dependencyName ->
                    project.findOdooModule(dependencyName)?.getAllFiles(PythonFileType.INSTANCE)
                }

        val files = if (includeModule) dependencyFiles + moduleDirectory.getAllFiles(PythonFileType.INSTANCE) else dependencyFiles

        GlobalSearchScope.filesScope(project, files)
    } else throw IllegalArgumentException("VirtualFile should be a directory, got $moduleDirectory")

    fun getAllModelNames(project: Project): List<String> = ReadAction.compute<List<String>, RuntimeException> {
        try {
            FileBasedIndex.getInstance().getAllKeys(NAME, project).toList()
        } catch (e: IndexNotReadyException) {
            emptyList()
        }
    }

    fun getModelInfo(
            project: Project,
            modelName: String,
            moduleName: String,
            addonPaths: List<String>,
    ): OdooModelNameIndexItem? {
        val moduleVF = try {
            FilenameIndex.getVirtualFilesByName(moduleName, true, ProjectScope.getAllScope(project))
                    .firstOrNull { it.parent.path in addonPaths }
        } catch (_: IndexNotReadyException) {
            return null
        }
        if (moduleVF == null) return null

        val scope = GlobalSearchScope.FilesScope.filesScope(project, moduleVF.getAllFiles(PythonFileType.INSTANCE))

        return try {
            FileBasedIndex.getInstance().getValues(NAME, modelName, scope)
                    .singleOrNull()
        } catch (_: IndexNotReadyException) {
            null
        }
    }

    fun getModelInfos(
            project: Project,
            modelName: String,
            moduleRoot: VirtualFile? = null,
            altScope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): List<OdooModelNameIndexItem> = ReadAction.compute<List<OdooModelNameIndexItem>, RuntimeException> {
        val scope = if (moduleRoot != null) getModuleDependencyScope(project, moduleRoot)
                    else GlobalSearchScope.projectScope(project).intersectWith(altScope)

        try {
            FileBasedIndex.getInstance().getValues(NAME, modelName, scope)
        } catch (_: IndexNotReadyException) {
            emptyList()
        }
    }

    fun getModelName(pyClass: PyClass, containingFile: VirtualFile? = null): String? = ReadAction.compute<String, RuntimeException> {
        try {
            val virtualFile = containingFile ?: pyClass.containingFile.virtualFile
            val map = FileBasedIndex.getInstance()
                    .getFileData(NAME, virtualFile, pyClass.project)
                    .takeIf { it.isNotEmpty() }
                    ?: return@compute null

            if (map.size == 1) map.keys.first() else {
                val classRangeInFile = pyClass.textRangeInParent
                map.firstNotNullOfOrNull { (modelName, item) ->
                    if (classRangeInFile.contains(item.modelNameOffset)) modelName else null
                }
            }
        } catch (_: IndexNotReadyException) {
            null
        }
    }

    /**
     * Get the [PyClass] of the model with [modelName] in [module]
     */
    fun getModel(modelName: String, module: String, project: Project): PyClass? = ReadAction.compute<PyClass?, RuntimeException>(ThrowableComputable {
        val moduleVF = project.findOdooModule(module) ?: return@ThrowableComputable null

        val modelVF = try {
            FileBasedIndex.getInstance().getContainingFiles(NAME, modelName, GlobalSearchScope.projectScope(project))
                    .singleOrNull { virtualFile -> VfsUtil.isAncestor(moduleVF, virtualFile, true) }
                    ?: return@ThrowableComputable null
        } catch (_: IndexNotReadyException) {
            return@ThrowableComputable null
        }

        (PsiManager.getInstance(project).findFile(modelVF) as? PyFile)?.topLevelClasses?.firstOrNull { pyClass ->
            pyClass.hasModelName(modelName)
        }
    })

    fun findModelsByName(
            project: Project,
            name: String,
            moduleRoot: VirtualFile? = null,
            altScope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): List<PyClass> = computeReadAction {
        val scope = if (moduleRoot != null) getModuleDependencyScope(project, moduleRoot).intersectWith(altScope)
                            else GlobalSearchScope.projectScope(project).intersectWith(altScope)

        val files = try {
            FileBasedIndex.getInstance().getContainingFiles(NAME, name, scope)
        } catch (e: IndexNotReadyException) {
            emptyList()
        }

        if (files.isEmpty()) return@computeReadAction emptyList<PyClass>()
        val psiManager = PsiManager.getInstance(project)

        files.filter { it.isValid }.flatMapNotNull { virtualFile ->
            (psiManager.findFile(virtualFile) as? PyFile)?.topLevelClasses?.filter { pyClass ->
                pyClass.hasModelName(name)
            }
        }
    }
}