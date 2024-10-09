package com.github.allepilli.odoodevelopmentplugin.extensions

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.intellij.psi.PsiFile

/**
 * This function can be computationally expensive, it is advised to only use this function when the [addonPaths] have not been set in [GeneralSettingsConfigurable][com.github.allepilli.odoodevelopmentplugin.settings.general.GeneralSettingsConfigurable]
 * @see getContainingModuleName instead
 */
fun PsiFile.slowGetContainingModuleName(): String? {
    var dir = parent ?: return null

    while (dir.findFile(Constants.MANIFEST_FILE_WITH_EXT) == null) {
        dir = dir.parent ?: return null
    }

    return dir.name
}