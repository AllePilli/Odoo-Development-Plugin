package com.github.allepilli.odoodevelopmentplugin.services

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * When called on EDT use:
 * ```
 * val app = ApplicationManager.getApplication()
 * app.executeOnPooledThread {
 *     app.runReadAction {
 *         project.service<OdooVersionManager>().getVersion()
 *     }
 * }
 * ```
 */
@Service(Service.Level.PROJECT)
class OdooVersionManager(private val project: Project) {
    companion object {
        private val VERSION_REGEX = """(?:saas-)?(\d{1,2}\.\d)""".toRegex()
        fun getVersionString(branchName: String): String? = VERSION_REGEX.find(branchName)
                ?.groupValues
                ?.getOrNull(1)
                ?: branchName
                        .takeIf { it.startsWith("master-") }
                        ?.let { "master" }
    }

    fun getVersion(virtualFile: VirtualFile): String? {
        val repositoryManager = VcsRepositoryManager.getInstance(project)
        val branchName = repositoryManager.getRepositoryForFile(virtualFile)
                ?.currentBranchName
                ?: return null
        return getVersionString(branchName)
    }

    fun getVersion(): String? = VcsRepositoryManager.getInstance(project)
            .getRepositories()
            .firstOrNull { repository ->
                // find the odoo base repository
                repository.presentableUrl.endsWith("odoo")
            }
            ?.currentBranchName
            ?.let(::getVersionString)
}