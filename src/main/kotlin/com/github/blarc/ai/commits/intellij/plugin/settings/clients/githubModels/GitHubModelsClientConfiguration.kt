package com.github.blarc.ai.commits.intellij.plugin.settings.clients.githubModels

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class GitHubModelsClientConfiguration : LLMClientConfiguration(
    "GitHub Models",
    "gpt-4o-mini",
    "0.7"
) {
    @Attribute
    var timeout: Int = 30
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null
    var topP = 0.95

    companion object {
        const val CLIENT_NAME = "GitHub Models"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GITHUB.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return GitHubModelsClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return GitHubModelsClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return GitHubModelsClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = GitHubModelsClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.timeout = timeout
        copy.topP = topP
        copy.tokenIsStored = tokenIsStored
        return copy
    }

    override fun panel() = GitHubModelsClientPanel(this)
}
