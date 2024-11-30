package com.github.blarc.ai.commits.intellij.plugin.settings.clients.huggingface

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import dev.langchain4j.model.huggingface.HuggingFaceModelName
import javax.swing.Icon

class HuggingFaceClientConfiguration : LLMClientConfiguration(
    "HuggingFace",
    HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT,
    "0.7"
) {

    @Attribute
    var timeout: Int = 30
    @Attribute
    var maxNewTokens: Int = 30
    @Attribute
    var waitForModel: Boolean = true
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null

    companion object {
        const val CLIENT_NAME = "HuggingFace"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.HUGGING_FACE.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return HuggingFaceClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {
        return HuggingFaceClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, commitMessage, project)
    }

    override fun cancelGenerateCommitMessage() {
        HuggingFaceClientService.getInstance().cancelGenerateCommitMessage()
    }

    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = HuggingFaceClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.tokenIsStored = tokenIsStored
        copy.timeout = timeout
        copy.waitForModel = waitForModel
        copy.maxNewTokens = maxNewTokens
        return copy
    }

    override fun panel() = HuggingFaceClientPanel(this)
}
