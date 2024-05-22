package com.github.allepilli.odoodevelopmentplugin.indexes.model_index

import com.github.allepilli.odoodevelopmentplugin.findModule
import com.github.allepilli.odoodevelopmentplugin.flatMapNotNull
import com.github.allepilli.odoodevelopmentplugin.getAllFiles
import com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndexUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Processor
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.ValueProcessor
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import java.io.DataInput
import java.io.DataOutput
import java.io.File

class OdooModelNameIndex: FileBasedIndexExtension<String, Int>() {
    private val indexer = OdooModelNameIndexer()

    override fun getName(): ID<String, Int> = OdooModelNameIndexUtil.NAME
    override fun dependsOnFileContent(): Boolean = true
    override fun getVersion(): Int = 1
    override fun getIndexer(): DataIndexer<String, Int, FileContent> = indexer
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<Int> = object: DataExternalizer<Int> {
        override fun save(out: DataOutput, value: Int) = DataInputOutputUtil.writeINT(out, value)
        override fun read(input: DataInput): Int = DataInputOutputUtil.readINT(input)
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter =
            object: DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE) {
                override fun acceptInput(file: VirtualFile): Boolean {
                    val path = file.path.split(File.separatorChar)

                    return ("addons" in path || "enterprise" in path) &&
                            "tests" !in path &&
                            !file.nameWithoutExtension.startsWith("__")
                }
            }
}

object OdooModelNameIndexUtil {
    val NAME = ID.create<String, Int>("OdooModelNameIndex")

    fun processAllNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) =
            FileBasedIndex.getInstance().processAllKeys(NAME, processor, scope, filter)

    fun processOffsets(name: String, processor: ValueProcessor<Int>, scope: GlobalSearchScope, inFile: VirtualFile? = null, filter: IdFilter? = null) =
            FileBasedIndex.getInstance().processValues(NAME, name, inFile, processor, scope, filter)

    fun getModuleDependencyScope(project: Project, moduleDirectory: VirtualFile, includeModule: Boolean = true) = if (moduleDirectory.isDirectory) {
        val dependencyFiles = ModuleDependencyIndexUtil.findAllDependencies(project, moduleDirectory.name)
                .flatMapNotNull { dependencyName ->
                    findModule(dependencyName, project)?.getAllFiles(PythonFileType.INSTANCE)
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

    fun findModelsByName(
            project: Project,
            name: String,
            moduleRoot: VirtualFile? = null,
            altScope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): List<PyClass> = ReadAction.compute<List<PyClass>, RuntimeException>(ThrowableComputable {
        val scope = if (moduleRoot != null) getModuleDependencyScope(project, moduleRoot)
                            else GlobalSearchScope.projectScope(project).intersectWith(altScope)

        val files = try {
            FileBasedIndex.getInstance().getContainingFiles(NAME, name, scope)
        } catch (e: IndexNotReadyException) {
            emptyList()
        }

        if (files.isEmpty()) return@ThrowableComputable emptyList<PyClass>()
        val psiManager = PsiManager.getInstance(project)

        files.filter { it.isValid }.flatMapNotNull { virtualFile ->
            (psiManager.findFile(virtualFile) as? PyFile)?.topLevelClasses?.filter { pyClass ->
                var matches = name == pyClass.findClassAttribute("_name", true, null)?.findAssignedValue()
                        ?.let { (it as? PyStringLiteralExpression)?.stringValue }

                if (!matches) {
                    matches = pyClass.findClassAttribute("_inherit", true, null)?.findAssignedValue()?.let {
                        when (it) {
                            is PyStringLiteralExpression -> name == it.stringValue
                            is PyListLiteralExpression -> name == it.elements
                                    .filterIsInstance<PyStringLiteralExpression>()
                                    .firstOrNull()
                                    ?.stringValue

                            else -> false
                        }
                    } == true
                }

                matches
            }
        }
    })
}