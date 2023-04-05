package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.exception.OpenAIAPIException
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.OpenAIService
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.swing.JPasswordField

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val tokenPasswordField = JPasswordField()
    private val verifyLabel = JBLabel()
    private val promptTextArea = JBTextArea()
    init {
        promptTextArea.wrapStyleWord = true
        promptTextArea.lineWrap = true
        promptTextArea.isEditable = false
    }
    override fun createPanel() = panel {

        row {
            cell(tokenPasswordField)
                .label(message("settings.openAIToken"))
                .bindText(
                    { AppSettings.instance.getOpenAIToken().orEmpty() },
                    { AppSettings.instance.saveOpenAIToken(it)}
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
            comboBox(AppSettings.instance.prompts.keys.toList(), AppSettingsListCellRenderer())
                .label(message("settings.prompt"))
                .bindItem(AppSettings.instance::currentPrompt.toNullableProperty())
                .onChanged { promptTextArea.text = AppSettings.instance.prompts[it.item] }
        }
        row {
            cell(promptTextArea)
                .bindText(
                    { AppSettings.instance.getPrompt("") },
                    {  }
                )
                .align(Align.FILL)
                .resizableColumn()
        }.resizableRow()
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
                    }
                    catch (e: OpenAIAPIException) {
                        verifyLabel.text = message("settings.verify.invalid", e.statusCode)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                    catch (e: Exception) {
                        verifyLabel.text = message("settings.verify.invalid", "Unknown")
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                }
            }
        }

    }
}