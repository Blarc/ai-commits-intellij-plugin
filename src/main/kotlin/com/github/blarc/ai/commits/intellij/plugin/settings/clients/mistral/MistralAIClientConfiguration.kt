package com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral;

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import dev.langchain4j.model.mistralai.MistralAiChatModelName
import kotlinx.coroutines.Job
import javax.swing.Icon

class MistralAIClientConfiguration : LLMClientConfiguration(
    "MistralAI"
) {
    @Attribute
    var modelId: String = MistralAiChatModelName.OPEN_MISTRAL_7B.toString()
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

    override fun getSharedState(): LLMClientSharedState {
        return MistralAIClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return MistralAIClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return MistralAIClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = MistralAIClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.token = token
        copy.topP = topP
        copy.maxTokens = maxTokens
        return copy
    }

    override fun panel() = MistralAIClientPanel(this)
}
