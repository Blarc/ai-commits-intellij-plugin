package com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import dev.langchain4j.model.qianfan.QianfanChatModelNameEnum
import javax.swing.Icon

class QianfanClientConfiguration : LLMClientConfiguration(
    "Qianfan",
    QianfanChatModelNameEnum.ERNIE_SPEED_128K.modelName,
    "0.7"
) {
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

    companion object {
        const val CLIENT_NAME = "Qianfan"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.QIANFAN
    }

    override fun getSharedState(): QianfanClientSharedState {
        return QianfanClientSharedState.getInstance()
    }

    override fun generateCommitMessage(prompt: String, project: Project, commitMessage: CommitMessage) {
        return QianfanClientService.getInstance().generateCommitMessage(this, prompt, project, commitMessage)
    }

    // Model names are retrieved from Enum and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = QianfanClientConfiguration()
        copy.id = id
        copy.apiKey = apiKey
        copy.secretKey = secretKey
        copy.name = name
        copy.host = host
        copy.modelId = modelId
        copy.temperature = temperature
        copy.apiKeyIsStored = apiKeyIsStored
        copy.secretKeyIsStored = secretKeyIsStored
        return copy
    }

    override fun panel() = QianfanClientPanel(this)
}
