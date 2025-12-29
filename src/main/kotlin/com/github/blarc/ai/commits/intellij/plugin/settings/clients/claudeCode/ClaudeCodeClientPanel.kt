package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class ClaudeCodeClientPanel private constructor(
    private val clientConfiguration: ClaudeCodeClientConfiguration,
    val service: ClaudeCodeClientService
) : LLMClientPanel(clientConfiguration) {

    private val cliPathTextField = JBTextField()
    private val timeoutTextField = JBTextField()

    constructor(configuration: ClaudeCodeClientConfiguration) : this(configuration, ClaudeCodeClientService.getInstance())

    override fun create() = panel {
        nameRow()
        cliPathRow()
        timeoutRow()
        modelRow()
        verifyRow()
    }

    private fun Panel.cliPathRow() {
        row {
            label(message("settings.claudeCode.cliPath"))
                .widthGroup("label")
            cell(cliPathTextField)
                .bindText(clientConfiguration::cliPath)
                .align(Align.FILL)
                .resizableColumn()
                .comment(message("settings.claudeCode.cliPath.comment"))
            button(message("settings.claudeCode.browse")) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                val chooser = com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, null, null)
                chooser?.let {
                    cliPathTextField.text = it.path
                }
            }.widthGroup("button")
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
                .comment(message("settings.claudeCode.timeout.comment"))
        }
    }

    private fun Panel.modelRow() {
        row {
            label(message("settings.claudeCode.model"))
                .widthGroup("label")
            cell(modelComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem({ clientConfiguration.modelId }, {
                    if (it != null) {
                        clientConfiguration.modelId = it
                    }
                })
                .onApply {
                    // Explicitly capture typed value from editable combobox
                    // bindItem doesn't reliably capture typed values not in the dropdown
                    modelComboBox.item?.let { clientConfiguration.modelId = it }
                }
                .align(Align.FILL)
                .resizableColumn()
                .comment(message("settings.claudeCode.model.comment"))
        }
    }

    override fun verifyConfiguration() {
        clientConfiguration.cliPath = cliPathTextField.text
        clientConfiguration.timeout = timeoutTextField.text.toIntOrNull() ?: 120
        clientConfiguration.modelId = modelComboBox.item ?: ""

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}