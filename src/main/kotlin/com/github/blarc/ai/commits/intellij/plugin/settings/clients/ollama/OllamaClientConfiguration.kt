package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
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
    @Attribute
    var topK: Int? = null
    @Attribute
    var topP: Double? = null
    @Attribute
    var numCtx: Int? = null
    @Attribute
    var numPredict: Int? = null

    companion object {
        const val CLIENT_NAME = "Ollama"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.OLLAMA.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return OllamaClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return OllamaClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    override fun cancelGenerateCommitMessage() {
        OllamaClientService.getInstance().cancelGenerateCommitMessage()
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
        copy.topP = topP
        copy.topK = topK
        copy.numCtx = numCtx
        copy.numPredict = numPredict
        return copy
    }

    override fun panel() = OllamaClientPanel(this)
}
