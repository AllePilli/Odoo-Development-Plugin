package com.github.allepilli.odoodevelopmentplugin.services

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
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

    fun getVersion(virtualFile: VirtualFile): OdooVersion? {
        val repositoryManager = VcsRepositoryManager.getInstance(project)
        val branchName = repositoryManager.getRepositoryForFile(virtualFile)
                ?.currentBranchName
                ?: kotlin.run {
                    logger<OdooVersionManager>().warn("Could not find Odoo version for file: ${virtualFile.path}")
                    return null
                }
        return getVersionString(branchName)?.let(OdooVersion.Companion::from)
    }

    fun getVersion(): OdooVersion? = VcsRepositoryManager.getInstance(project)
            .getRepositories()
            .firstOrNull { repository ->
                // find the odoo base repository
                repository.presentableUrl.endsWith("odoo")
            }
            ?.currentBranchName
            ?.let(::getVersionString)
            ?.let(OdooVersion.Companion::from)

    /**
     * @return true if the current Odoo version >= [minVersion] false if the version could not be found or the condition fails
     */
    fun versionAtLeast(virtualFile: VirtualFile, minVersion: OdooVersion): Boolean {
        val version = getVersion(virtualFile) ?: return false
        return version >= minVersion
    }

    fun versionAtLeast(virtualFile: VirtualFile, minVersion: String): Boolean =
            versionAtLeast(virtualFile, OdooVersion.from(minVersion))

    /**
     * @return true if the current Odoo version >= [minVersion] false if the version could not be found or the condition fails
     */
    fun versionAtLeast(minVersion: OdooVersion): Boolean {
        val version = getVersion() ?: return false
        return version >= minVersion
    }

    fun versionAtLeast(minVersion: String): Boolean = versionAtLeast(OdooVersion.from(minVersion))
}

sealed class OdooVersion: Comparable<OdooVersion> {
    companion object {
        fun from(versionString: String) = if (versionString == "master") Master else Numbered(versionString)
    }

    abstract val version: String

    data object Master: OdooVersion() {
        override val version: String
            get() = "master"

        override fun compareTo(other: OdooVersion): Int = when (other) {
            is Master -> 0
            is Numbered -> 1
        }
    }

    class Numbered(override val version: String): OdooVersion() {
        val majorVersion: Int
        val minorVersion: Int

        init {
            if ('.' in version) {
                val (major, minor) = version.split('.', limit = 2)
                majorVersion = major.toInt()
                minorVersion = minor.toInt()
            } else {
                majorVersion = version.toInt()
                minorVersion = 0
            }
        }

        override fun compareTo(other: OdooVersion): Int = when (other) {
            is Master -> -1
            is Numbered -> when (val majorComparison = majorVersion.compareTo(other.majorVersion)) {
                0 -> minorVersion.compareTo(other.minorVersion)
                else -> majorComparison
            }
        }
    }
}