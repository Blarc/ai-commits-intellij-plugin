package com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import dev.langchain4j.model.mistralai.MistralAiChatModelName
import kotlinx.coroutines.Job
import javax.swing.Icon

class MistralAIClientConfiguration : LlmClientConfiguration(
    "MistralAI",
    MistralAiChatModelName.OPEN_MISTRAL_7B.toString()
) {
    @Attribute
    var temperature: String = "0.7"
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    @Attribute
    var topP: Double? = null
    @Attribute
    var maxTokens: Int? = null

    companion object {
        const val CLIENT_NAME = "MistralAI"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.MISTRAL.getThemeBasedIcon()
    }

    override fun getSharedState(): LlmClientSharedState {
        return MistralAIClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return MistralAIClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return MistralAIClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
        val copy = MistralAIClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.cleanupRegex = cleanupRegex
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.token = token
        copy.topP = topP
        copy.maxTokens = maxTokens
        return copy
    }

    override fun panel() = MistralAIClientPanel(this)
}
