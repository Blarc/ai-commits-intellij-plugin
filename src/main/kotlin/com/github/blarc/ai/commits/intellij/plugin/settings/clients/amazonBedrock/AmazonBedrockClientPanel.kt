package com.github.blarc.ai.commits.intellij.plugin.settings.clients.amazonBedrock;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import software.amazon.awssdk.regions.Region

class AmazonBedrockClientPanel private constructor(
    private val clientConfiguration: AmazonBedrockClientConfiguration,
    val service: AmazonBedrockClientService
) : LLMClientPanel(clientConfiguration) {

    private val accessKeyIdField = JBTextField()
    private val accessKeyPasswordField = JBPasswordField()
    private val regionComboBox = ComboBox(Region.regions().toTypedArray())
    private val maxOutputTokensTextField = JBTextField()
    private val topPTextField = JBTextField()
    private val topKTextField = JBTextField()

    constructor(configuration: AmazonBedrockClientConfiguration) : this(configuration, AmazonBedrockClientService.getInstance())

    override fun create() = panel {
        nameRow()
        modelIdRow(commentKey = "settings.amazonBedrock.modelId.comment")
        temperatureRow()
        timeoutRow(clientConfiguration::timeout)
        accessKeyIdRow()
        accessKeyRow()
        regionRow()
        maxTokens()
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        topKRow(topKTextField, clientConfiguration::topK.toNullableProperty())
        verifyRow()
    }

    private fun Panel.accessKeyIdRow() {
        row {
            label(message("settings.amazonBedrock.accessKeyId"))
                .widthGroup("label")
            cell(accessKeyIdField)
                .bindText(clientConfiguration::accessKeyId.toNonNullableProperty(""))
                .emptyText(message("settings.amazonBedrock.accessKeyId.example"))
                .resizableColumn()
                .align(Align.FILL)
        }
    }

    private fun Panel.accessKeyRow() {
        row {
            label(message("settings.amazonBedrock.accessKey"))
                .widthGroup("label")
            cell(accessKeyPasswordField)
                .bindText(getter = { "" }, setter = {
                    AmazonBedrockClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.accessKeyIsStored) message("settings.llmClient.token.stored") else message("settings.amazonBedrock.accessKey.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.amazonBedrock.accessKey.comment"), 50)
        }
    }

    private fun Panel.regionRow() {
        row {
            label(message("settings.amazonBedrock.region"))
                .widthGroup("label")
            cell(regionComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(clientConfiguration::region.toNullableProperty())
                .align(Align.FILL)

        }
    }

    private fun Panel.maxTokens() {
        row {
            label(message("settings.amazonBedrock.maxOutputTokens"))
                .widthGroup("label")
            cell(maxOutputTokensTextField)
                .bindText({ clientConfiguration.maxOutputTokens?.toString() ?: "" }, { s -> clientConfiguration::maxOutputTokens.set(s.toInt()) })
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
                .resizableColumn()
        }
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.accessKeyId = accessKeyIdField.text
        clientConfiguration.accessKey = String(accessKeyPasswordField.password)
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.topK = topKTextField.text.toIntOrNull()
        clientConfiguration.maxOutputTokens = maxOutputTokensTextField.text.toIntOrNull()
        clientConfiguration.region = regionComboBox.selectedItem as Region
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
