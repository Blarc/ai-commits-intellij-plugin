package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class OpenAiClientConfiguration : LLMClientConfiguration(
    "OpenAI",
    "gpt-3.5-turbo"
) {

    @Attribute
    var temperature: String = "0.7"
    @Attribute
    var host: String = "https://api.openai.com/v1"
    @Attribute
    var timeout: Int = 30
    @Attribute
    var organizationId: String? = null
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    @Attribute
    var topP: Double? = null

    companion object {
        const val CLIENT_NAME = "OpenAI"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.OPEN_AI.getThemeBasedIcon()
    }

    override fun getSharedState(): OpenAiClientSharedState {
        return OpenAiClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return OpenAiClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return OpenAiClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = OpenAiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.host = host
        copy.timeout = timeout
        copy.modelId = modelId
        copy.organizationId = organizationId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.topP = topP
        return copy
    }

    override fun panel() = OpenAiClientPanel(this)
}
