package com.github.blarc.ai.commits.intellij.plugin.settings.clients.codexCli

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientPanel
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.DefaultComboBoxModel

class CodexCliClientPanel private constructor(
    private val clientConfiguration: CodexCliClientConfiguration,
    val service: CodexCliClientService
) : LlmClientPanel(clientConfiguration) {

    private val cliPathTextField = TextFieldWithBrowseButton()
    private val timeoutTextField = JBTextField()
    private val reasoningLevelComboBox = ComboBox(CodexCliClientConfiguration.REASONING_LEVELS.toTypedArray())

    constructor(configuration: CodexCliClientConfiguration) : this(configuration, CodexCliClientService.getInstance())

    override fun create() = panel {
        updateReasoningLevelsForModel(clientConfiguration.modelId)
        nameRow()
        cliPathRow()
        timeoutRow()
        modelRow()
        reasoningLevelRow()
        cleanUpRegexRow()
        verifyRow()
    }

    private fun Panel.cliPathRow() {
        row {
            label(message("settings.codexCli.cliPath"))
                .widthGroup("label")
            cell(cliPathTextField)
                .bindText(clientConfiguration::cliPath)
                .align(Align.FILL)
                .resizableColumn()
                .comment(message("settings.codexCli.cliPath.comment"))
                .applyToComponent {
                    addBrowseFolderListener(
                        message("settings.codexCli.cliPath"),
                        null,
                        null,
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                    )
                }
        }
    }

    private fun Panel.timeoutRow() {
        row {
            label(message("settings.llmClient.timeout"))
                .widthGroup("label")
            cell(timeoutTextField)
                .bindIntText(clientConfiguration::timeout)
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.codexCli.timeout.comment"))
        }
    }

    private fun Panel.modelRow() {
        row {
            label(message("settings.codexCli.model"))
                .widthGroup("label")
            cell(modelComboBox)
                .applyToComponent {
                    isEditable = true
                    addItemListener { updateReasoningLevelsForModel(modelComboBox.item) }
                }
                .bindItem({ clientConfiguration.modelId }, {
                    if (it != null) {
                        clientConfiguration.modelId = it
                    }
                })
                .onApply {
                    // Capture typed value from editable combobox.
                    modelComboBox.item?.let { clientConfiguration.modelId = it }
                }
                .align(Align.FILL)
                .resizableColumn()
                .comment(message("settings.codexCli.model.comment"))
        }
    }

    private fun Panel.reasoningLevelRow() {
        row {
            label(message("settings.codexCli.reasoningLevel"))
                .widthGroup("label")
            cell(reasoningLevelComboBox)
                .bindItem({ clientConfiguration.reasoningLevel }, {
                    if (it != null) {
                        clientConfiguration.reasoningLevel = it
                    }
                })
                .align(Align.FILL)
                .resizableColumn()
                .comment(message("settings.codexCli.reasoningLevel.comment"))
        }
    }

    private fun updateReasoningLevelsForModel(model: Any?) {
        val modelName = model as? String
        val levels = if (modelName == "gpt-5.1-codex-mini") {
            listOf("Medium", "High")
        } else {
            CodexCliClientConfiguration.REASONING_LEVELS
        }

        val current = reasoningLevelComboBox.item as? String
        reasoningLevelComboBox.model = DefaultComboBoxModel(levels.toTypedArray())

        val fallback = if (CodexCliClientConfiguration.DEFAULT_REASONING_LEVEL in levels) {
            CodexCliClientConfiguration.DEFAULT_REASONING_LEVEL
        } else {
            levels.first()
        }
        reasoningLevelComboBox.item = if (current in levels) current else fallback
    }

    override fun verifyConfiguration() {
        clientConfiguration.cliPath = cliPathTextField.text.trim()
        clientConfiguration.timeout = timeoutTextField.text.toIntOrNull() ?: 120
        clientConfiguration.modelId = modelComboBox.item ?: ""
        clientConfiguration.reasoningLevel = reasoningLevelComboBox.item ?: CodexCliClientConfiguration.DEFAULT_REASONING_LEVEL

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
