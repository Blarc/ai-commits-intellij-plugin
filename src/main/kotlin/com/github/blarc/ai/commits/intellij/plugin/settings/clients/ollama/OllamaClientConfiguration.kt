package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vcs.ui.CommitMessage
import javax.swing.Icon

class OllamaClientConfiguration : LLMClientConfiguration(
    "Ollama",
    "http://localhost:11434/",
    null,
    30,
    "llama3",
    "0.7"
) {

    companion object {
        const val CLIENT_NAME = "Ollama"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.OLLAMA
    }

    override fun getSharedState(): LLMClientSharedState {
        return OllamaClientSharedState.getInstance()
    }

    override fun generateCommitMessage(prompt: String, commitMessage: CommitMessage) {
        return OllamaClientService.getInstance().generateCommitMessage(this, prompt, commitMessage)
    }

    override fun getRefreshModelsFunction() = fun (cb: ComboBox<String>) {
        OllamaClientService.getInstance().refreshModels(this, cb)
    }

    override fun clone(): LLMClientConfiguration {
        val copy = OllamaClientConfiguration()
        copy.id = id
        copy.name = name
        copy.host = host
        copy.proxyUrl = proxyUrl
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override fun panel() = OllamaClientPanel(this)
}
