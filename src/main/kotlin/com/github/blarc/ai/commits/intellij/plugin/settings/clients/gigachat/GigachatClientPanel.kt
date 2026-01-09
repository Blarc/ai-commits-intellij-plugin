package com.github.blarc.ai.commits.intellij.plugin.settings.clients.gigachat

import chat.giga.model.Scope
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.*
import org.jetbrains.kotlin.fir.resolve.scopeSessionKey

class GigachatClientPanel(private val clientConfiguration: GigachatClientConfiguration) :
    LLMClientPanel(clientConfiguration) {
    private val tokenPasswordField = JBPasswordField()

    private val authUrlComboBox = ComboBox(clientConfiguration.getAuthUrls().toTypedArray())

    private val scopeComboBox = ComboBox(Scope.values())

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::apiUrl.toNullableProperty(), "settings.gigachat.host")
        authUrlRow(clientConfiguration::authUrl.toNullableProperty())
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        scopeRow()
        temperatureRow()
        verifyRow()
    }

    open fun Panel.scopeRow(labelKey: String = "settings.llmClient.modelId") {
        row {
            label(message("settings.gigachat.scope"))
                .widthGroup("label")
            cell(scopeComboBox)
                .applyToComponent {
                    isEditable = false
                }
                .bindItem(
                    { clientConfiguration.scope },
                    {
                        if (it != null) {
                            clientConfiguration.scope = it
                        }
                    })
                .align(Align.FILL)
                .onApply { clientConfiguration.scope = scopeComboBox.item }
        }
    }

    private fun Panel.authUrlRow(property: MutableProperty<String?>, labelKey: String = "settings.gigachat.authUrl") {
        row {
            label(message("settings.gigachat.authUrl"))
                .widthGroup("label")
            cell(authUrlComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(property)
                .align(Align.FILL)
                .onApply { clientConfiguration.addAuthUrl(authUrlComboBox.item) }
        }
    }

    override fun getRefreshModelsFunction() = fun() {
        clientConfiguration.apiUrl = hostComboBox.item
        clientConfiguration.authUrl = authUrlComboBox.item
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.scope = scopeComboBox.item
        GigachatClientService.getInstance().refreshModels(clientConfiguration, modelComboBox, verifyLabel)
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    GigachatClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(
                    if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message(
                        "settings.gigachat.token.example"
                    )
                )
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.gigachat.token.comment"), 50)
        }
    }

    override fun verifyConfiguration() {

        clientConfiguration.apiUrl = hostComboBox.item
        clientConfiguration.authUrl = authUrlComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.scope = scopeComboBox.item

        GigachatClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}