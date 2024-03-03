package com.github.blarc.ai.commits.intellij.plugin

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlin.time.Duration.Companion.seconds
import com.aallam.openai.api.model.Model
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long


@Service(Service.Level.APP)
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
        AppSettings.instance.openAIModelIds=getOpenAIModels(openAI).map { it.id.id }
    }

    @Throws(Exception::class)
    suspend fun verifyOpenAIConfiguration(host: String, token: String, proxy: String?, socketTimeout: String){

        val config = OpenAIConfig(
                token,
                host = host.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
                proxy = proxy?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) },
                timeout = Timeout(socket = socketTimeout.toInt().seconds)
        )
        val openAI = OpenAI(config)
        getOpenAIModels(openAI)
    }

    @Throws(Exception::class)
    suspend fun getOpenAIModels(openAI: OpenAI): List<Model> {
        try
        {
            return openAI.models()
        }
        catch (_: Exception) {
            // Fallback to list of models
        }
        try {
            val requester = openAI.javaClass.getDeclaredField("requester").apply { isAccessible = true }.get(openAI)
            val httpClient = requester.javaClass.getDeclaredField("httpClient").apply { isAccessible = true }.get(requester) as HttpClient;
            val response: HttpResponse = httpClient.get{url(path = "models") }
            val jsonArray = response.body<JsonArray>()
            val models = mutableListOf<Model>()
            jsonArray.forEach {
                models.add(Model(
                        created = it.jsonObject["created"]?.jsonPrimitive?.long ?: 0,
                        id = ModelId(it.jsonObject["id"]?.jsonPrimitive?.content ?: ""),
                        ownedBy = "system"
                ))
            }
            return models
        }
        catch (_: Exception) {
            throw Exception("Failed to retrieve models from OpenAI API.")
        }
    }
}
