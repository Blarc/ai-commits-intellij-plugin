package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClient
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientTable
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.Prompt
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.PromptTable
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.*
import java.util.*

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val llmClientTable = LLMClientTable()
    private lateinit var llmClientToolbarDecorator: ToolbarDecorator
    private lateinit var llmClientComboBox: ComboBox<LLMClient>
    private val promptTable = PromptTable()
    private lateinit var toolbarDecorator: ToolbarDecorator
    private lateinit var promptComboBox: ComboBox<Prompt>

    override fun createPanel() = panel {

        row {
            label(message("settings.llmClient")).widthGroup("labelPrompt")
            llmClientComboBox = comboBox(AppSettings2.instance.llmClients, AICommitsListCellRenderer())
                .bindItem(getter = AppSettings2.instance::getActiveLLMClient) {
                    it?.let {
                        AppSettings2.instance.setActiveLlmClient(it)
                    }
                }.widthGroup("input")
                .component
        }
        row {
            llmClientToolbarDecorator = ToolbarDecorator.createDecorator(llmClientTable.table)
                .setAddAction {
                    llmClientTable.addLlmClient()
                }
                .setEditAction {
                    llmClientTable.editLlmClient()
                }
                .setRemoveAction {
                    llmClientTable.removeLlmClient()?.let {
                        llmClientComboBox.removeItem(it)
                    }
                }
                .disableUpDownActions()

            cell(llmClientToolbarDecorator.createPanel())
                .align(Align.FILL)
        }.resizableRow()

        row {
            label(message("settings.locale")).widthGroup("labelPrompt")
            comboBox(Locale.getAvailableLocales()
                .distinctBy { it.displayLanguage }
                .sortedBy { it.displayLanguage },
                AICommitsListCellRenderer()
            )
                .widthGroup("input")
                .bindItem(AppSettings2.instance::locale.toNullableProperty())

            browserLink(message("settings.more-prompts"), AICommitsBundle.URL_PROMPTS_DISCUSSION.toString())
                .align(AlignX.RIGHT)
        }
        row {
            label(message("settings.prompt")).widthGroup("labelPrompt")
            promptComboBox = comboBox(AppSettings2.instance.prompts.values, AICommitsListCellRenderer())
                .bindItem(AppSettings2.instance::activePrompt.toNullableProperty())
                .widthGroup("input")
                .component
        }
        row {
            toolbarDecorator = ToolbarDecorator.createDecorator(promptTable.table)
                .setAddAction {
                    promptTable.addPrompt().let {
                        promptComboBox.addItem(it)
                    }
                }
                .setEditAction {
                    promptTable.editPrompt()?.let {
                        val editingSelected = promptComboBox.selectedItem == it.first
                        promptComboBox.removeItem(it.first)
                        promptComboBox.addItem(it.second)

                        if (editingSelected) {
                            promptComboBox.selectedItem = it.second
                        }
                    }
                }
                .setEditActionUpdater {
                    updateActionAvailability(CommonActionsPanel.Buttons.EDIT)
                    true
                }
                .setRemoveAction {
                    promptTable.removePrompt()?.let {
                        promptComboBox.removeItem(it)
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
        return super.isModified() || promptTable.isModified() || llmClientTable.isModified()
    }

    override fun apply() {
        // TODO @Blarc
        // AppSettings2.instance.getActiveLLMClient().hosts.add(hostComboBox.item)
        promptTable.apply()
        llmClientTable.apply()
        super.apply()
    }

    override fun reset() {
        promptTable.reset()
        llmClientTable.reset()
        super.reset()
    }

}
