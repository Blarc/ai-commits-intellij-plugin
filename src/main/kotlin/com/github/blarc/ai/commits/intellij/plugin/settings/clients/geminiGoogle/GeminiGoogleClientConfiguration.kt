package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiGoogle

import GeminiGoogleClientService
import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import javax.swing.Icon

class GeminiGoogleClientConfiguration : LLMClientConfiguration(
    "Gemini Google",
    "gemini-1.5-pro",
    "0.7"
) {
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    var topK = 40
    var topP = 0.95

    companion object {
        const val CLIENT_NAME = "Gemini Google"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GEMINI_GOOGLE.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return GeminiGoogleClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return GeminiGoogleClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    override fun cancelGenerateCommitMessage() {
        GeminiGoogleClientService.getInstance().cancelGenerateCommitMessage()
    }

    // Model names are hard-coded and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = GeminiGoogleClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.token = token
        copy.topK = topK
        copy.topP = topP
        return copy
    }

    override fun panel() = GeminiGoogleClientPanel(this)

}
