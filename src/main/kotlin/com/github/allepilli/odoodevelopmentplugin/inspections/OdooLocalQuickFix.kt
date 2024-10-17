package com.github.allepilli.odoodevelopmentplugin.inspections

import com.github.allepilli.odoodevelopmentplugin.bundles.StringsBundle
import com.intellij.codeInspection.LocalQuickFix

interface OdooLocalQuickFix: LocalQuickFix {
    val nameKey: String

    override fun getName(): String = StringsBundle.message(nameKey)
    override fun getFamilyName(): String = name
}