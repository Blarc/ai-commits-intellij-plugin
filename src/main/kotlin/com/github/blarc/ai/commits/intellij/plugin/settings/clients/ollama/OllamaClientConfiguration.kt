package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import javax.swing.Icon

class OllamaClientConfiguration : LLMClientConfiguration(
    "Ollama",
    "llama3",
    "0.7"
) {

    @Attribute
    var host: String = "http://localhost:11434/"
    @Attribute
    var timeout: Int = 30

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

    override fun generateCommitMessage(prompt: String, project: Project, commitMessage: CommitMessage) {
        return OllamaClientService.getInstance().generateCommitMessage(this, prompt, project, commitMessage)
    }

    override fun getRefreshModelsFunction() = fun (cb: ComboBox<String>) {
        OllamaClientService.getInstance().refreshModels(this, cb)
    }

    override fun clone(): LLMClientConfiguration {
        val copy = OllamaClientConfiguration()
        copy.id = id
        copy.name = name
        copy.host = host
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override fun panel() = OllamaClientPanel(this)
}
