package com.github.blarc.ai.commits.intellij.plugin

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service


@Service
class OpenAIService {

    companion object {
        val instance: OpenAIService
            get() = ApplicationManager.getApplication().getService(OpenAIService::class.java)
    }

    suspend fun generateCommitMessage(prompt: String, completions: Int): String {
        val openAI = OpenAI(AppSettings.instance.getOpenAIConfig())

        val chatCompletionRequest = ChatCompletionRequest(
            ModelId(AppSettings.instance.openAIModelId),
            listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            temperature = AppSettings.instance.openAITemperature.toDouble(),
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.0,
            maxTokens = 200,
            n = completions
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices[0].message.content ?: "API returned an empty response."
    }

    suspend fun refreshOpenAIModelIds() {
        val openAI = OpenAI(AppSettings.instance.getOpenAIConfig())
        AppSettings.instance.openAIModelIds=openAI.models().map { it.id.id }
    }

    @Throws(Exception::class)
    suspend fun verifyOpenAIConfiguration(host: String, token: String, proxy: String?){

        val config = OpenAIConfig(
                token,
                host = host.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
                proxy = proxy?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) }
        )
        val openAI = OpenAI(config)
        openAI.models()
    }
}
