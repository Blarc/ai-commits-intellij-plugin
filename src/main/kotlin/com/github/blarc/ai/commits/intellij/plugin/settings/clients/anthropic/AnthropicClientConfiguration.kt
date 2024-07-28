package com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic;

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import dev.langchain4j.model.anthropic.AnthropicChatModelName
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
    @Transient
    var version: String? = null
    @Transient
    var beta: String? = null
    @Attribute
    var timeout: Int = 30

    companion object {
        const val CLIENT_NAME = "Anthropic"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.ANTHROPIC
    }

    override fun getSharedState(): LLMClientSharedState {
        return AnthropicClientSharedState.getInstance()
    }

    override fun generateCommitMessage(prompt: String, project: Project, commitMessage: CommitMessage) {
        return AnthropicClientService.getInstance().generateCommitMessage(this, prompt, project, commitMessage)
    }

    override fun getRefreshModelsFunction() = null

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
        return copy
    }

    override fun panel() = AnthropicClientPanel(this)
}
