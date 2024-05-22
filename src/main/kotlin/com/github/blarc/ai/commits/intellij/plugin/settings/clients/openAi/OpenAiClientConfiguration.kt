package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.ui.CommitMessage
import javax.swing.Icon

class OpenAiClientConfiguration(displayName: String = "OpenAI") : LLMClientConfiguration(
    displayName,
    "https://api.openai.com/v1",
    null,
    30,
    "gpt-3.5-turbo",
    "0.7"
) {
    override fun getIcon(): Icon {
        return Icons.OPEN_AI
    }

    override fun getHosts(): Set<String> {
        return service<OpenAiClientService>().hosts
    }

    override fun getModelIds(): Set<String> {
        return service<OpenAiClientService>().modelIds
    }

    override fun generateCommitMessage(prompt: String, commitMessage: CommitMessage) {
        return OpenAiClientService.getInstance().generateCommitMessage(this, prompt, commitMessage)
    }

    // Model names are retrieved from Enum and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = OpenAiClientConfiguration(displayName)
        copy.host = host
        copy.proxyUrl = proxyUrl
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override fun panel() = OpenAiClientPanel(this)
}
