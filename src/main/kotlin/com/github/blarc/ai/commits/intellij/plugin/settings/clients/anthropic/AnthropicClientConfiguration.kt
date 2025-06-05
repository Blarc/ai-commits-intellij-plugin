package com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import dev.langchain4j.model.anthropic.AnthropicChatModelName
import kotlinx.coroutines.Job
import javax.swing.Icon

class AnthropicClientConfiguration : LLMClientConfiguration(
    "Anthropic",
    AnthropicChatModelName.CLAUDE_2_1.toString(),
    "0.7"
) {
    @Attribute
    var host: String = "https://api.anthropic.com/v1/"
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    @Attribute
    var version: String? = null
    @Attribute
    var beta: String? = null
    @Attribute
    var timeout: Int = 30
    @Attribute
    var topK: Int? = null
    @Attribute
    var topP: Double? = null

    companion object {
        const val CLIENT_NAME = "Anthropic"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.ANTHROPIC.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return AnthropicClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return AnthropicClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return AnthropicClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = AnthropicClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.version = version
        copy.beta = beta
        copy.timeout = timeout
        copy.topK = topK
        copy.topP = topP
        return copy
    }

    override fun panel() = AnthropicClientPanel(this)
}
