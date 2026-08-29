package com.github.blarc.ai.commits.intellij.plugin.settings.clients.githubModels

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class GitHubModelsClientConfiguration : LlmClientConfiguration(
    "GitHub Models",
    "gpt-4o-mini"
) {
    @Attribute
    var temperature: String = "0.7"
    @Attribute
    var timeout: Int = 30
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    @Attribute
    var topP: Double? = null

    companion object {
        const val CLIENT_NAME = "GitHub Models"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GITHUB.getThemeBasedIcon()
    }

    override fun getSharedState(): LlmClientSharedState {
        return GitHubModelsClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return GitHubModelsClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return GitHubModelsClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
        val copy = GitHubModelsClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.cleanupRegex = cleanupRegex
        copy.temperature = temperature
        copy.timeout = timeout
        copy.topP = topP
        copy.tokenIsStored = tokenIsStored
        return copy
    }

    override fun panel() = GitHubModelsClientPanel(this)
}
