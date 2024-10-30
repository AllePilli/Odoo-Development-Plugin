package com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.extensions.isOdooModuleDirectory
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.python.PythonFileType

private val NAME: ID<String, Set<String>> = ID.create("com.github.allepilli.odoodevelopmentplugin.indexes.module_dependency_index.ModuleDependencyIndex")

class ModuleDependencyIndex: FileBasedIndexExtension<String, Set<String>>() {
    private val moduleDependencyDataIndexer = ModuleDependencyDataIndexer()

    override fun getName(): ID<String, Set<String>> = NAME
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor()
    override fun getValueExternalizer(): DataExternalizer<Set<String>> = ModuleDependencyIndexValueExternalizer()
    override fun getInputFilter(): FileBasedIndex.InputFilter = object : DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE) {
        override fun acceptInput(file: VirtualFile): Boolean =
                file.nameWithoutExtension == Constants.MANIFEST_FILE_NAME && file.parent.isOdooModuleDirectory()
    }
    override fun dependsOnFileContent(): Boolean = true
    override fun getVersion(): Int = 1
    override fun getIndexer(): DataIndexer<String, Set<String>, FileContent> = moduleDependencyDataIndexer
}

object ModuleDependencyIndexUtil {
    fun getAllModuleNames(project: Project): Set<String> =
            ReadAction.compute<Set<String>, RuntimeException> {
                try {
                    FileBasedIndex.getInstance()
                            .getAllKeys(NAME, project)
                            .toSet()
                } catch (e: IndexNotReadyException) {
                    emptySet()
                }
            }

    fun findDirectDependencies(
            project: Project,
            moduleName: String,
            scope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): Set<String> = ReadAction.compute<Set<String>, RuntimeException> {
        try {
            FileBasedIndex.getInstance()
                    .getValues(NAME, moduleName, GlobalSearchScope.projectScope(project).intersectWith(scope))
                    .singleOrNull()
                    ?: emptySet()
        } catch (e: IndexNotReadyException) {
            emptySet()
        }
    }

    fun findAllDependencies(
            project: Project,
            moduleName: String,
            scope: GlobalSearchScope = ProjectScope.getAllScope(project),
    ): Set<String> = buildSet {
        val found = mutableSetOf<String>()
        found.addAll(findDirectDependencies(project, moduleName, scope))

        while (found.isNotEmpty()) {
            addAll(found)
            val depends = found.flatMap { findDirectDependencies(project, it, scope) }.toSet()
            found.clear()
            found.addAll(depends)
            found.removeAll(this)
        }
    }
}
