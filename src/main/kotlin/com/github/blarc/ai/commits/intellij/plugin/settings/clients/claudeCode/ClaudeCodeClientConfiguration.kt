package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class ClaudeCodeClientConfiguration : LLMClientConfiguration(
    "Claude Code",
    "",  // No default model - uses CLI's configured model
    ""   // No temperature - CLI doesn't support it
) {

    @Attribute
    var cliPath: String = ""  // Empty means auto-detect

    @Attribute
    var timeout: Int = 120  // Longer default for CLI execution

    companion object {
        const val CLIENT_NAME = "Claude Code"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.CLAUDE_CODE.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return ClaudeCodeClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return ClaudeCodeClientService.getInstance().generateCommitMessageCli(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return ClaudeCodeClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = ClaudeCodeClientConfiguration()
        copy.id = id
        copy.name = name
        copy.cliPath = cliPath
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override fun panel() = ClaudeCodeClientPanel(this)
}