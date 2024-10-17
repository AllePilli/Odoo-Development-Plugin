package com.github.allepilli.odoodevelopmentplugin.inspections

import com.github.allepilli.odoodevelopmentplugin.Constants
import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.github.allepilli.odoodevelopmentplugin.fixtures.BaseOdooTestCase
import com.github.allepilli.odoodevelopmentplugin.inspections.redundant_module_dependency.RedundantModuleDependencyInspection
import com.github.allepilli.odoodevelopmentplugin.test_util.OdooSettingsUtil
import com.github.allepilli.odoodevelopmentplugin.test_util.assertNotNull
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.testFramework.IndexingTestUtil

class RedundantModuleDependencyInspectionTest: BaseOdooTestCase() {
    companion object {
        private val quickFixText = StringsBundle.message("QFIX.remove.redundant.module.dependency")
    }

    private lateinit var root: VirtualFile

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(RedundantModuleDependencyInspection())

        root = copyMyProject()

        OdooSettingsUtil.setRelativeAddonsPaths(project, "root/odoo/addons", "root/odoo/enterprise")
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.project)
    }

    fun `test Redundant module at the end of list`() {
        doQuickFixTest("test_module_1")
    }

    fun `test Redundant module at the start of list`() {
        doQuickFixTest("test_module_2")
    }

    fun `test Redundant module in middle of list`() {
        doQuickFixTest("test_module_3")
    }

    private fun doQuickFixTest(moduleName: String) {
        val manifestPath = "enterprise/$moduleName/${Constants.MANIFEST_FILE_WITH_EXT}"
        val manifest: VirtualFile = root.findFile(manifestPath).assertNotNull { "Can not find the manifest file in module '$moduleName'." }

        myFixture.openFileInEditor(manifest)

        val action = myFixture.getAvailableIntentions("root/$manifestPath")
                .firstOrNull { action -> action.text == quickFixText }
                .assertNotNull { "Can not find action with text: $quickFixText"  }

        myFixture.launchAction(action)

        checkResultByRelativePath("__manifest__.after.py")
    }
}