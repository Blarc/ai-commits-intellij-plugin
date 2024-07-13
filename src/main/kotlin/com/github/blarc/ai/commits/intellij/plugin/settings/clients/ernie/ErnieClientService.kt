package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ernie

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.qianfan.QianfanChatModel
import dev.langchain4j.model.qianfan.QianfanChatModelNameEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class ErnieClientService(private val cs: CoroutineScope) : LLMClientService<ErnieClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): ErnieClientService = service()
    }

    override suspend fun buildChatModel(client: ErnieClientConfiguration): ChatLanguageModel {
        val apiKey = client.apiKey.nullize(true) ?: retrieveToken(client.apiKeyId)?.toString(true)
        val secretKey = client.secretKey.nullize(true) ?: retrieveToken(client.secretKeyId)?.toString(true)

        val builder = QianfanChatModel.builder()
            .baseUrl(client.host)
            .apiKey(apiKey)
            .secretKey(secretKey)
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
        // Fix https://github.com/langchain4j/langchain4j/pull/1426. Remove this 'if' statement when langchain4j releases a new version that resolves this issue.
        if (client.modelId == QianfanChatModelNameEnum.ERNIE_SPEED_128K.modelName){
            builder.endpoint("ernie-speed-128k")
        }

        return builder.build()
    }

    private fun saveToken(token: String, title: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(title), token)
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }
    fun saveApiKey(client: ErnieClientConfiguration, apiKey: String) {
        saveToken(apiKey, client.apiKeyId)
        client.apiKeyIsStored = true
    }
    fun saveSecretKey(client: ErnieClientConfiguration, secretKey: String) {
        saveToken(secretKey, client.secretKeyId)
        client.secretKeyIsStored = true
    }
}
