package com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import dev.langchain4j.community.model.qianfan.QianfanChatModelNameEnum
import kotlinx.coroutines.Job
import javax.swing.Icon

class QianfanClientConfiguration : LlmClientConfiguration(
    "Qianfan",
    QianfanChatModelNameEnum.ERNIE_SPEED_128K.modelName
) {
    @Attribute
    var temperature: String = "0.7"
    @Attribute
    var host: String = "https://aip.baidubce.com"
    @Attribute
    var apiKeyIsStored : Boolean = false
    @Attribute
    var secretKeyIsStored: Boolean = false
    @Transient
    var apiKey: String = ""
    @Transient
    var secretKey: String = ""
    @Attribute
    var topP: Double? = null

    companion object {
        const val CLIENT_NAME = "Qianfan"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.QIANFAN.getThemeBasedIcon()
    }

    override fun getSharedState(): QianfanClientSharedState {
        return QianfanClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return QianfanClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return QianfanClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LlmClientConfiguration {
        val copy = QianfanClientConfiguration()
        copy.id = id
        copy.apiKey = apiKey
        copy.secretKey = secretKey
        copy.name = name
        copy.host = host
        copy.modelId = modelId
        copy.cleanupRegex = cleanupRegex
        copy.temperature = temperature
        copy.apiKeyIsStored = apiKeyIsStored
        copy.secretKeyIsStored = secretKeyIsStored
        copy.topP = topP
        return copy
    }

    override fun panel() = QianfanClientPanel(this)
}
