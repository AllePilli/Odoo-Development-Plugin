package com.github.allepilli.odoodevelopmentplugin.actions

import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunConfiguration
import com.github.allepilli.odoodevelopmentplugin.execution.OdooRunType
import com.github.allepilli.odoodevelopmentplugin.execution.tests.OdooTestConfiguration
import com.intellij.execution.RunManager
import com.intellij.execution.actions.RunConfigurationsComboBoxAction
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.ui.laf.darcula.ui.ToolbarComboWidgetUiSizes
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.actionSystem.impl.Utils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsActions
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

class RunTypeSelector: ToggleRunTypeAction(OdooRunType.entries.map { it.presentableName }), CustomComponentAction {
    private var selectedRunConfiguration: RunConfiguration? = null
    private var selectedRunType: OdooRunType?
        get() = when (selectedRunConfiguration) {
            is OdooRunConfiguration -> (selectedRunConfiguration as? OdooRunConfiguration)?.runType
            is OdooTestConfiguration -> (selectedRunConfiguration as? OdooTestConfiguration)?.runType
            else -> null
        }
        set(value) {
            if (value == null) return
            when (selectedRunConfiguration) {
                is OdooRunConfiguration -> (selectedRunConfiguration as? OdooRunConfiguration)?.runType = value
                is OdooTestConfiguration -> (selectedRunConfiguration as? OdooTestConfiguration)?.runType = value
            }
        }

    override fun onStateSelected(state: String) {
        selectedRunType = OdooRunType.entries.first { it.presentableName == state }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        e.presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)

        val actionManager: ActionManager = ApplicationManager.getApplication().serviceIfCreated<ActionManager>() ?: return
        val runConfigurationAction = actionManager.getAction("RunConfiguration") as? RunConfigurationsComboBoxAction ?: return
        runConfigurationAction.update(e)

        e.project?.let { RunManager.getInstanceIfCreated(it) }?.selectedConfiguration?.configuration?.let { configuration ->
            if (configuration is OdooRunConfiguration || configuration is OdooTestConfiguration) {
                e.presentation.isVisible = true
                selectedRunConfiguration = configuration
                selectedState = selectedRunType?.presentableName ?: ""
            } else {
                e.presentation.isVisible = false
                selectedRunConfiguration = null
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun createCustomComponent(presentation: Presentation, place: String): JComponent =
            object : ActionButtonWithText(this, presentation, place, { JBUI.size(16, JBUI.CurrentTheme.RunWidget.toolbarHeight()) }) {
                private val marginInsets = JBInsets(0, 10, 0, 6)

                init {
                    foreground = JBUI.CurrentTheme.RunWidget.FOREGROUND
                    setHorizontalTextAlignment(SwingConstants.LEFT)
                }

                override fun getMargins(): Insets = marginInsets
                override fun iconTextSpace(): Int = ToolbarComboWidgetUiSizes.gapAfterLeftIcons
                override fun shallPaintDownArrow(): Boolean = true
                override fun getText(): @NlsActions.ActionText String = selectedState
            }
}

abstract class ToggleRunTypeAction(val states: List<String>): ToggleAction() {
    companion object {
        private val popupContentBorder = JBEmptyBorder(JBUI.insets(2, 10))
        private val minimumPopupSize = JBDimension(135, 0)
        private val listCellRenderer = ListCellRenderer<String> { _, value, _, _, _ ->
            JBLabel(value ?: "").apply {
                border = JBEmptyBorder(JBUI.insets(0, 5))
            }
        }
    }

    var selectedState = states.first()

    override fun isSelected(e: AnActionEvent): Boolean = Toggleable.isSelected(e.presentation)
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (!state) return

        val component = e.inputEvent?.component as? JComponent ?: return
        val start = IdeEventQueue.getInstance().popupTriggerTime
        val popup = createPopup(e)

        Utils.showPopupElapsedMillisIfConfigured(start, popup.content)
        popup.showUnderneathOf(component)
    }

    private fun createPopup(e: AnActionEvent): JBPopup = JBPopupFactory.getInstance().createPopupChooserBuilder(states)
            .setItemChosenCallback { chosenItem ->
               selectedState = chosenItem
               onStateSelected(chosenItem)
            }
            .setRenderer(listCellRenderer)
            .setMinSize(minimumPopupSize)
            .createPopup()
            .apply {
                Disposer.register(this) {
                    // When Disposed
                    Toggleable.setSelected(e.presentation, false)
                }

                content.border = popupContentBorder
            }

    abstract fun onStateSelected(state: String)
}