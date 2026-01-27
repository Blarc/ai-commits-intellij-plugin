package com.github.blarc.ai.commits.intellij.plugin.settings.clients.codexCli

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class CodexCliClientConfiguration : LlmClientConfiguration(
    "Codex CLI",
    DEFAULT_MODEL
) {

    @Attribute
    var cliPath: String = ""

    @Attribute
    var timeout: Int = 120 // Longer default for CLI execution.

    @Attribute
    var reasoningLevel: String = DEFAULT_REASONING_LEVEL

    companion object {
        const val CLIENT_NAME = "Codex CLI"
        const val DEFAULT_MODEL = "gpt-5.2-codex"
        const val DEFAULT_REASONING_LEVEL = "Medium"
        val REASONING_LEVELS = listOf(
            "Low",
            "Medium",
            "High",
            "Extra High"
        )
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.CODEX.getThemeBasedIcon()
    }

    override fun getSharedState(): LlmClientSharedState {
        return CodexCliClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return CodexCliClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return CodexCliClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
        val copy = CodexCliClientConfiguration()
        copy.id = id
        copy.name = name
        copy.cleanupRegex = cleanupRegex
        copy.cliPath = cliPath
        copy.timeout = timeout
        copy.modelId = modelId
        copy.reasoningLevel = reasoningLevel
        return copy
    }

    override fun panel() = CodexCliClientPanel(this)

    override fun afterSerialization() {
        if (modelId.isBlank()) {
            modelId = DEFAULT_MODEL
        }
        if (reasoningLevel.isBlank()) {
            reasoningLevel = DEFAULT_REASONING_LEVEL
        }
    }
}
