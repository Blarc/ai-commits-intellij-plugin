package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.exception.OpenAIAPIException
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.OpenAIService
import com.github.blarc.ai.commits.intellij.plugin.settings.prompt.Prompt
import com.github.blarc.ai.commits.intellij.plugin.settings.prompt.PromptTable
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JScrollPane

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val tokenPasswordField = JPasswordField()
    private val verifyLabel = JBLabel()
    private val promptTable = PromptTable()
    private lateinit var toolbarDecorator: ToolbarDecorator
    private lateinit var promptComboBox: Cell<ComboBox<Prompt>>
    override fun createPanel() = panel {

        row {
            cell(tokenPasswordField)
                    .label(message("settings.openAIToken"))
                    .bindText(
                            { AppSettings.instance.getOpenAIToken().orEmpty() },
                            { AppSettings.instance.saveOpenAIToken(it) }
                    )
                    .align(Align.FILL)
                    .resizableColumn()
                    .focused()
            button(message("settings.verifyToken")) {
                verifyToken()
            }.align(AlignX.RIGHT)
        }
        row {
            comment(message("settings.openAITokenComment"))
                    .align(AlignX.LEFT)
            cell(verifyLabel)
                    .align(AlignX.RIGHT)
        }
        row {
            comboBox(Locale.getAvailableLocales().toList().sortedBy { it.displayName }, AppSettingsListCellRenderer())
                    .label(message("settings.locale"))
                    .bindItem(AppSettings.instance::locale.toNullableProperty())
        }
        row {
            promptComboBox = comboBox(AppSettings.instance.prompts.values, AppSettingsListCellRenderer())
                    .label(message("settings.prompt"))
                    .bindItem(AppSettings.instance::currentPrompt.toNullableProperty())
        }
        row {
            toolbarDecorator = ToolbarDecorator.createDecorator(promptTable.table)
                    .setAddAction {
                        promptTable.addPrompt().let {
                            promptComboBox.component.addItem(it)
                        }
                    }
                    .setEditAction {
                        promptTable.editPrompt()?.let {
                            promptComboBox.component.removeItem(it.first)
                            promptComboBox.component.addItem(it.second)
                        }
                    }
                    .setEditActionUpdater {
                        updateActionAvailability(CommonActionsPanel.Buttons.EDIT)
                        true
                    }
                    .setRemoveAction {
                        promptTable.removePrompt()?.let {
                            promptComboBox.component.removeItem(it)
                        }
                    }
                    .setRemoveActionUpdater {
                        updateActionAvailability(CommonActionsPanel.Buttons.REMOVE)
                        true
                    }
                    .disableUpDownActions()

            cell(toolbarDecorator.createPanel())
                    .align(Align.FILL)
        }.resizableRow()

        row {
            browserLink(message("settings.report-bug"), AICommitsBundle.URL_BUG_REPORT.toString())
        }
    }

    private fun updateActionAvailability(action: CommonActionsPanel.Buttons) {
        val selectedRow = promptTable.table.selectedRow
        val selectedPrompt = promptTable.table.items[selectedRow]
        toolbarDecorator.actionsPanel.setEnabled(action, selectedPrompt.canBeChanged)
    }

    override fun isModified(): Boolean {
        return super.isModified() || promptTable.isModified()
    }

    override fun apply() {
        promptTable.apply()
        super.apply()
    }

    override fun reset() {
        promptTable.reset()
        super.reset()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun verifyToken() {
        runBackgroundableTask(message("settings.verify.running")) {
            if (tokenPasswordField.password.isEmpty()) {
                verifyLabel.icon = AllIcons.General.InspectionsError
                verifyLabel.text = message("settings.verify.token-is-empty")
            } else {
                verifyLabel.icon = AllIcons.General.InlineRefreshHover
                verifyLabel.text = message("settings.verify.running")

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        OpenAIService.instance.verifyToken(String(tokenPasswordField.password))
                        verifyLabel.text = message("settings.verify.valid")
                        verifyLabel.icon = AllIcons.General.InspectionsOK
                    } catch (e: OpenAIAPIException) {
                        verifyLabel.text = message("settings.verify.invalid", e.statusCode)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    } catch (e: Exception) {
                        verifyLabel.text = message("settings.verify.invalid", "Unknown")
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                }
            }
        }

    }
}