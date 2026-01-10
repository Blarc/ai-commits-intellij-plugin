package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class ClaudeCodeClientConfiguration : LlmClientConfiguration(
    "Claude Code",
    "",  // No default model - uses CLI's configured model
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

    override fun getSharedState(): LlmClientSharedState {
        return ClaudeCodeClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return ClaudeCodeClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return ClaudeCodeClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
        val copy = ClaudeCodeClientConfiguration()
        copy.id = id
        copy.name = name
        copy.cleanupRegex = cleanupRegex
        copy.cliPath = cliPath
        copy.timeout = timeout
        copy.modelId = modelId
        return copy
    }

    override fun panel() = ClaudeCodeClientPanel(this)
}