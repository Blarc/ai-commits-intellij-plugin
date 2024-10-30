package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiVertex

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import javax.swing.Icon

// Can not rename this class because of backwards compatibility
// with persistent component - AppSettings2
class GeminiClientConfiguration : LLMClientConfiguration(
    "Gemini Vertex",
    "gemini-pro",
    "0.7"
) {
    @Attribute
    var projectId: String = "project-id"
    @Attribute
    var location: String = "us-central1"

    companion object {
        const val CLIENT_NAME = "Gemini Vertex"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GEMINI_VERTEX
    }

    override fun getSharedState(): LLMClientSharedState {
        return GeminiVertexClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return GeminiVertexClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    // Model names are hard-coded and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = GeminiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.projectId = projectId
        copy.location = location
        return copy
    }

    override fun panel() = GeminiVertexClientPanel(this)


}
