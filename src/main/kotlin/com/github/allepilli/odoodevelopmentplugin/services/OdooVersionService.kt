package com.github.allepilli.odoodevelopmentplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepositoryManager

@Service(Service.Level.PROJECT)
class OdooVersionService(private val project: Project) {
    /**
     * @return the odoo version the of the current git branch of the odoo community repository
     * or null if the odoo community repository could not be found or if
     * [git4idea.repo.GitRepository.getCurrentBranch] fails.
     *
     * example: "17.0-l10n_ro_etransport_edi-peal" => "17.0"
     */
    fun getVersion(): String? {
        val repos = GitRepositoryManager.getInstance(project).repositories
        val odooRepo = repos.find { repo ->
            repo.remotes.any { remote ->
                remote.name == "origin" && remote.urls.any { url ->
                    url == "git@github.com:odoo/odoo.git"
                }
            }
        } ?: return null

        val currentBranchName = odooRepo.currentBranch?.name ?: return null

        return currentBranchName.replaceFirst("saas-", "")
                .takeWhile { it != '-' }
    }
}