package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openrouter

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import javax.swing.Icon

class OpenRouterClientConfiguration : LLMClientConfiguration(
    "OpenRouter",
    "nousresearch/hermes-3-llama-3.1-405b:free",
    "0.5"
) {
    @Attribute
    var host: String = "https://openrouter.ai/api/v1"
    @Attribute
    var timeout: Int = 30
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null

    companion object {
        const val CLIENT_NAME = "OpenRouter"
    }

    override fun getClientName(): String = CLIENT_NAME

    override fun getClientIcon(): Icon = Icons.OPEN_AI // You may want to create a custom icon for OpenRouter

    override fun getSharedState(): LLMClientSharedState = OpenRouterClientSharedState.getInstance()

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        OpenRouterClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = OpenRouterClientConfiguration()
        copy.id = id
        copy.name = name
        copy.host = host
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        return copy
    }

    override fun panel() = OpenRouterClientPanel(this)
}