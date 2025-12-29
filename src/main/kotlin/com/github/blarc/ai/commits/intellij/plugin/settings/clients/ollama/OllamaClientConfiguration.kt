package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class OllamaClientConfiguration : LlmClientConfiguration(
    "Ollama",
    "llama3"
) {

    @Attribute
    var temperature: String = "0.7"
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

    override fun getSharedState(): LlmClientSharedState {
        return OllamaClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return OllamaClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return OllamaClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
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
