package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiApi

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import javax.swing.Icon

class GeminiApiClientConfiguration : LLMClientConfiguration(
    "GeminiApi",
    "gemini-1.5-pro",
    "0.7"
) {
    @Attribute
    var apiKey: String = "api-key"

    companion object {
        const val CLIENT_NAME = "GeminiApi"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GEMINI
    }

    override fun getSharedState(): LLMClientSharedState {
        return GeminiApiClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return GeminiApiClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    // Model names are hard-coded and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = GeminiApiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.apiKey = apiKey
        return copy
    }

    override fun panel() = GeminiApiClientPanel(this)

}