package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Transient
import javax.swing.Icon

class OpenAiClientConfiguration : LLMClientConfiguration(
    "OpenAI",
    "https://api.openai.com/v1",
    null,
    30,
    "gpt-3.5-turbo",
    "0.7"
) {
    var organizationId: String? = null
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null

    companion object {
        const val CLIENT_NAME = "OpenAI"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.OPEN_AI
    }

    override fun getSharedState(): OpenAiClientSharedState {
        return OpenAiClientSharedState.getInstance()
    }

    override fun generateCommitMessage(prompt: String, commitMessage: CommitMessage) {
        return OpenAiClientService.getInstance().generateCommitMessage(this, prompt, commitMessage)
    }

    // Model names are retrieved from Enum and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = OpenAiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.host = host
        copy.proxyUrl = proxyUrl
        copy.timeout = timeout
        copy.modelId = modelId
        copy.organizationId = organizationId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        return copy
    }

    override fun panel() = OpenAiClientPanel(this)
}
