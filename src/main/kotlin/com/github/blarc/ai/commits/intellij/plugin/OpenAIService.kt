package com.github.blarc.ai.commits.intellij.plugin

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

@Service
class OpenAIService {

    private val openAI: OpenAI = OpenAI(AppSettings.instance.getOpenAIToken().orEmpty())

    companion object {
        const val model = "gpt-3.5-turbo"
        val instance: OpenAIService
            get() = ApplicationManager.getApplication().getService(OpenAIService::class.java)
    }

    private fun getPrompt(locale: String, diff: String) =
        "Write an insightful but concise Git commit message in a complete sentence in present tense for the following diff without prefacing it with anything, the response must be in the language ${locale}:\\n${diff}"

    @OptIn(BetaOpenAI::class)
    suspend fun generateCommitMessage(diff: String, completions: Int): String {
        val prompt = getPrompt(AppSettings.instance.locale.displayLanguage, diff)

        val chatCompletionRequest = ChatCompletionRequest(
            ModelId(model),
            listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            temperature = 0.7,
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.0,
            maxTokens = 200,
            n = completions
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices[0].message!!.content

    }
}