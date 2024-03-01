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
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import java.io.File

private val NAME: ID<String, Void> = ID.create("com.github.allepilli.odoodevelopmentplugin.indexes.model_index.OdooModelIndex")

class OdooModelIndex: ScalarIndexExtension<String>() {
    private val odooModelDataIndexer = OdooModelDataIndexer()

    override fun getName(): ID<String, Void> = NAME
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getVersion(): Int = 1
    override fun getInputFilter(): FileBasedIndex.InputFilter =
            object: DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE) {
                override fun acceptInput(file: VirtualFile): Boolean {
                    val path = file.path.split(File.separatorChar)

                    return ("addons" in path || "enterprise" in path) &&
                            "tests" !in path &&
                            !file.nameWithoutExtension.startsWith("__")
                }
            }

    override fun dependsOnFileContent(): Boolean = true
    override fun getIndexer(): DataIndexer<String, Void?, FileContent> = odooModelDataIndexer
}


object OdooModelIndexUtil {
    fun getModuleDependencyScope(project: Project, moduleDirectory: VirtualFile) = if (moduleDirectory.isDirectory) {
        val files = ModuleDependencyIndexUtil.findAllDependencies(project, moduleDirectory.name)
                .flatMapNotNull { dependencyName ->
                    findModule(dependencyName, project)?.getAllFiles(PythonFileType.INSTANCE)
                }

        GlobalSearchScope.filesScope(project, files)
    } else throw IllegalArgumentException("VirtualFile should be a directory, got $moduleDirectory")

    fun getAllModelNames(project: Project): List<String> =
            ReadAction.compute<List<String>, RuntimeException> {
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
            scope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): List<PyClass> = ReadAction.compute<List<PyClass>, RuntimeException>(ThrowableComputable {
        val completeScope = GlobalSearchScope.projectScope(project).intersectWith(scope).apply {
            if (moduleRoot != null) intersectWith(getModuleDependencyScope(project, moduleRoot))
        }

        val files = try {
            FileBasedIndex.getInstance().getContainingFiles(NAME, name, completeScope)
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
                    } ?: false
                }

                matches
            }
        }
    })
}
