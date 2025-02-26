package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientTable
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.Prompt
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.PromptTable
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.CoroutineScope
import java.util.*
import java.text.Collator

// Most of the settings are global, but we use project configurable to set isProjectSpecificLLMClient property
class AppSettingsConfigurable(val project: Project, cs: CoroutineScope) : BoundConfigurable(message("settings.general.group.title")) {

    private val projectSettings = project.service<ProjectSettings>()
    private val llmClientTable = LLMClientTable()
    private lateinit var llmClientConfigurationComboBox: ComboBox<LLMClientConfiguration>
    private var isProjectSpecificLLMClientCheckBox = JBCheckBox(message("settings.llmClient.projectSpecific"))
    private lateinit var llmClientToolbarDecorator: ToolbarDecorator
    private val promptTable = PromptTable(cs)
    private val isProjectSpecificPromptCheckBox = JBCheckBox(message("settings.prompt.projectSpecific"))
    private lateinit var toolbarDecorator: ToolbarDecorator
    private lateinit var promptComboBox: ComboBox<Prompt>

    override fun createPanel() = panel {

        row {
            label(message("settings.llmClient")).widthGroup("labelPrompt")
            llmClientConfigurationComboBox = comboBox(AppSettings2.instance.llmClientConfigurations, AICommitsListCellRenderer())
                .bindItem(getter = { projectSettings.getActiveLLMClientConfiguration() }, setter = { setActiveLLMClientConfiguration(it) })
                .widthGroup("input")
                .component
            cell(isProjectSpecificLLMClientCheckBox)
                .bindSelected(project.service<ProjectSettings>()::isProjectSpecificLLMClient)
            contextHelp(message("settings.llmClient.projectSpecific.contextHelp"))
                .align(AlignX.LEFT)
            checkBox(message("settings.llmClient.streamingResponse"))
                .bindSelected(AppSettings2.instance::useStreamingResponse)
            contextHelp(message("settings.llmClient.streamingResponse.contextHelp"))
                .align(AlignX.LEFT)
        }
        row {
            llmClientToolbarDecorator = ToolbarDecorator.createDecorator(llmClientTable.table)
                .setAddAction {
                    llmClientTable.addLlmClient().let {
                        llmClientConfigurationComboBox.addItem(it)
                    }
                }
                .setEditAction {
                    llmClientTable.editLlmClient()?.let {
                        val editingActive = llmClientConfigurationComboBox.selectedItem == it.first
                        llmClientConfigurationComboBox.removeItem(it.first)
                        llmClientConfigurationComboBox.addItem(it.second)

                        if (editingActive) {
                            llmClientConfigurationComboBox.selectedItem = it.second
                        }
                    }
                }
                .setRemoveAction {
                    llmClientTable.removeLlmClient()?.let {
                        llmClientConfigurationComboBox.removeItem(it)
                    }
                }
                .disableUpDownActions()

            cell(llmClientToolbarDecorator.createPanel())
                .align(Align.FILL)
        }.resizableRow()

        row {
            label(message("settings.locale")).widthGroup("labelPrompt")
            val ideLocale = AICommitsUtils.getIDELocale()

            // Configures locale-aware text comparator for proper language name sorting
            // Without a Collator, we would get incorrect sorting based on raw Unicode values
            val collator = Collator.getInstance(ideLocale).apply {
                strength = Collator.TERTIARY
                decomposition = Collator.CANONICAL_DECOMPOSITION
            }

            val locales = Locale.getAvailableLocales()
                .asSequence()
                .filter { it.language.isNotBlank() }
                .distinctBy { it.language }
                .sortedWith(compareBy(collator) { locale ->
                    locale.getDisplayLanguage(ideLocale)
                })
                .toList()

            comboBox(locales, AICommitsListCellRenderer())
                .widthGroup("input")
                .bindItem(
                    getter = { locales.find { it.language == projectSettings.locale.language } ?: Locale.ENGLISH },
                    setter = { setActiveLocale(it)}
                )

            contextHelp(message("settings.locale.contextHelp"))

            browserLink(message("settings.more-prompts"), AICommitsBundle.URL_PROMPTS_DISCUSSION.toString())
                .align(AlignX.RIGHT)
        }
        row {
            label(message("settings.prompt")).widthGroup("labelPrompt")
            promptComboBox = comboBox(AppSettings2.instance.prompts.values, AICommitsListCellRenderer())
                .bindItem(getter = { projectSettings.activePrompt }, { setActivePrompt(it) })
                .widthGroup("input")
                .component
            cell(isProjectSpecificPromptCheckBox)
                .bindSelected(project.service<ProjectSettings>()::isProjectSpecificPrompt)
            contextHelp(message("settings.prompt.projectSpecific.contextHelp"))
                .align(AlignX.LEFT)
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

    private fun setActiveLLMClientConfiguration(llmClientConfiguration: LLMClientConfiguration?) {
        llmClientConfiguration?.let {
            if (isProjectSpecificLLMClientCheckBox.isSelected) {
                project.service<ProjectSettings>().activeLlmClientId = it.id
            } else {
                AppSettings2.instance.activeLlmClientId = it.id
            }
        }
    }

    private fun setActivePrompt(prompt: Prompt?) {
        prompt?.let {
            if (isProjectSpecificPromptCheckBox.isSelected) {
                project.service<ProjectSettings>().activePrompt = it
            } else {
                AppSettings2.instance.activePrompt = it
            }
        }
    }

    private fun setActiveLocale(locale: Locale?) {
        locale?.let {
            if (isProjectSpecificPromptCheckBox.isSelected) {
                project.service<ProjectSettings>().locale = it
            } else {
                AppSettings2.instance.locale = it
            }
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
