package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import javax.swing.Icon

class OpenAiClientConfiguration : LLMClientConfiguration(
    "OpenAI",
    "gpt-3.5-turbo",
    "0.7"
) {

    @Attribute
    var host: String = "https://api.openai.com/v1"
    @Attribute
    var timeout: Int = 30
    @Attribute
    var proxyUrl: String? = null
    @Attribute
    var organizationId: String? = null
    @Attribute
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

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return OpenAiClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
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
