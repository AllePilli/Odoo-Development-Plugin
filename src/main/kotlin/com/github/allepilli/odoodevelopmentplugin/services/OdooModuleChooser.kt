package com.github.allepilli.odoodevelopmentplugin.services

import com.github.allepilli.odoodevelopmentplugin.OdooIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.SimpleColoredComponent
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.awt.Component
import java.awt.Font
import javax.swing.JList
import javax.swing.ListCellRenderer

@Service(Service.Level.APP)
class OdooModuleChooser {
    companion object {
        private class CellRenderer: SimpleColoredComponent(), ListCellRenderer<String> {
            private val font: Font

            init {
                val scheme = EditorColorsManager.getInstance().globalScheme
                font = scheme.getFont(EditorFontType.PLAIN)
                isOpaque = true
            }

            override fun getListCellRendererComponent(list: JList<out String>?, value: String?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                clear()

                icon = OdooIcons.odoo
                value?.let(::append)
                setFont(font)

                if (isSelected) {
                    background = list?.selectionBackground
                    foreground = list?.selectionForeground
                } else {
                    background = list?.background
                    foreground = list?.foreground
                }

                return this
            }
        }
    }

    fun selectModule(modules: List<String>): Promise<String> {
        if (ApplicationManager.getApplication().isUnitTestMode)
            return resolvedPromise(modules.first())

        val result = AsyncPromise<String>()

        // GUI
        DataManager.getInstance().dataContextFromFocusAsync.onSuccess { dataContext ->
            JBPopupFactory.getInstance().createPopupChooserBuilder(modules)
                    .setRenderer(CellRenderer())
                    .setTitle("Choose Module")
                    .setItemChosenCallback { result.setResult(it) }
                    .setNamerForFiltering { it }
                    .createPopup()
                    .showInBestPositionFor(dataContext)
        }

        return result
    }
}