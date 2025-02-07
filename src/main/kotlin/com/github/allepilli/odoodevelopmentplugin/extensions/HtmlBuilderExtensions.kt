package com.github.allepilli.odoodevelopmentplugin.extensions

import com.github.allepilli.odoodevelopmentplugin.HtmlUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder

fun HtmlBuilder.appendXmlCodeBlock(project: Project, textCreator: () -> String) =
        append(HtmlUtils.createXmlCodeBlock(project, textCreator))
