package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClient
import com.intellij.openapi.components.service
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.swing.Icon

class OllamaClient(displayName: String = "Ollama") : LLMClient(
    displayName,
    "http://localhost:11434/",
    null,
    30,
    "llama3",
    "0.7"
) {
    override fun getIcon(): Icon {
        return Icons.OLLAMA
    }

    override fun getHosts(): Set<String> {
        return service<OllamaClientService>().hosts
    }

    override fun getModelIds(): Set<String> {
        return service<OllamaClientService>().modelIds

    }

    override suspend fun generateCommitMessage(prompt: String): String {
        val ollama = OllamaChatModel.builder()
            .modelName(modelId)
            .temperature(temperature.toDouble())
            .timeout(Duration.ofSeconds(timeout.toLong()))
            .baseUrl(host)
            .build()

        val response = ollama.generate(
            listOf(
                UserMessage.from(
                    "user",
                    prompt
                )
            )
        )
        return response.content().text()

    }

    override fun getRefreshModelFunction(): (suspend () -> Unit) = {
        // Model names are set by the user.
        val ollamaModels = OllamaModels.builder()
            .timeout(Duration.ofSeconds(timeout.toLong()))
            .baseUrl(host)
            .build()

        val availableModels = withContext(Dispatchers.IO) {
            ollamaModels.availableModels()
        }

        service<OllamaClientService>().modelIds.addAll(availableModels.content()
            .map { it.name }
        )
    }

    override fun clone(): LLMClient {
        val copy = OllamaClient(displayName)
        copy.host = host
        copy.proxyUrl = proxyUrl
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override suspend fun verifyConfiguration(newHost: String, newProxy: String?, newTimeout: String, newModelId: String, newToken: String) {
        TODO("Not yet implemented")
    }

    override fun panel() = OllamaClientPanel(this)
}
