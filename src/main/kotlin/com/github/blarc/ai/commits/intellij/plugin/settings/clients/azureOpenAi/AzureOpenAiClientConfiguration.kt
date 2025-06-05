package com.github.blarc.ai.commits.intellij.plugin.settings.clients.azureOpenAi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class AzureOpenAiClientConfiguration : LLMClientConfiguration(
    CLIENT_NAME,
    "",
    "0.7"
) {

    @Attribute
    var host: String = ""
    @Attribute
    var timeout: Int = 30
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    @Attribute
    var topP: Double? = null

    companion object {
        const val CLIENT_NAME = "Azure OpenAI"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.AZURE_OPEN_AI.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return AzureOpenAiClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return AzureOpenAiClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return AzureOpenAiClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = AzureOpenAiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.host = host
        copy.timeout = timeout
        copy.tokenIsStored = tokenIsStored
        copy.topP = topP
        return copy
    }

    override fun panel() = AzureOpenAiClientPanel(this)
}
