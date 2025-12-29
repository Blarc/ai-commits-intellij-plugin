package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiVertex

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

// Can not rename this class because of backwards compatibility
// with persistent component - AppSettings2
class GeminiClientConfiguration : LLMClientConfiguration(
    "Gemini Vertex",
    "gemini-pro"
) {
    @Attribute
    var temperature: String = "0.7"
    @Attribute
    var projectId: String = "project-id"
    @Attribute
    var location: String = "us-central1"
    @Attribute
    var topK: Int? = null
    @Attribute
    var topP: Float? = null

    companion object {
        const val CLIENT_NAME = "Gemini Vertex"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GEMINI_VERTEX.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return GeminiVertexClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return GeminiVertexClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return GeminiVertexClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = GeminiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.projectId = projectId
        copy.location = location
        copy.topP = topP
        copy.topK = topK
        return copy
    }

    override fun panel() = GeminiVertexClientPanel(this)


}
