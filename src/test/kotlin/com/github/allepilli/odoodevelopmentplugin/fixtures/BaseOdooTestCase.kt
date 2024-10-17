package com.github.allepilli.odoodevelopmentplugin.fixtures

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/testData")
abstract class BaseOdooTestCase: BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData"

    fun checkResultByRelativePath(path: String) =
            myFixture.checkResultByFile("${this::class.simpleName}/expected_results/$path")

    fun copyMyProject(targetPath: String = "root") =
            myFixture.copyDirectoryToProject("${this::class.simpleName}/project", targetPath)
}