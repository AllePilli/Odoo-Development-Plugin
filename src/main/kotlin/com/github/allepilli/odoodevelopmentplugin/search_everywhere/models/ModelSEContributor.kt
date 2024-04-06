package com.github.allepilli.odoodevelopmentplugin.search_everywhere.models

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.ide.util.gotoByName.LanguageRef
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class ModelSEContributor(event: AnActionEvent) : AbstractGotoSEContributor(event) {
    override fun getGroupName(): String = "Models"
    override fun getSortWeight(): Int = 1
    override fun createModel(project: Project): FilteringGotoByModel<LanguageRef> = GotoModelModel(project)
}

class ModelSEContributorFactory: SearchEverywhereContributorFactory<Any> {
    override fun createContributor(event: AnActionEvent): SearchEverywhereContributor<Any> = ModelSEContributor(event)
}