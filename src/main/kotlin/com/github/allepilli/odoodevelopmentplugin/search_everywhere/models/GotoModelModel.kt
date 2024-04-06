package com.github.allepilli.odoodevelopmentplugin.search_everywhere.models

import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.ide.util.gotoByName.LanguageRef
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.ui.IdeUICustomization

class GotoModelModel(project: Project) : FilteringGotoByModel<LanguageRef>(project, listOf(ModelChooseByNameContributor())) {
    override fun getPromptText(): String = "Enter Model name"
    override fun getNotInMessage(): String = "No matches found in project"
    override fun getNotFoundMessage(): String = "No matches found"
    override fun willOpenEditor(): Boolean = true
    override fun loadInitialCheckBoxState(): Boolean = true
    override fun saveInitialCheckBoxState(state: Boolean) {}
    override fun getSeparators(): Array<String> = emptyArray()
    override fun getCheckBoxName(): String = IdeUICustomization.getInstance()
            .projectMessage("checkbox.include.non.project.items")

    override fun getFullName(element: Any): String? =
            if (element !is NavigationItem) throw AssertionError("$element of ${element.javaClass} in $this of $javaClass")
            else element.name


    override fun filterValueFor(item: NavigationItem?): LanguageRef? = null
}