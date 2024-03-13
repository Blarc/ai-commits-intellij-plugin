package com.github.blarc.ai.commits.intellij.plugin

import com.aallam.openai.api.chat.*
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service


@Service(Service.Level.APP)
class OpenAIService {

    companion object {
        val instance: OpenAIService
            get() = ApplicationManager.getApplication().getService(OpenAIService::class.java)
    }

    suspend fun generateCommitMessage(prompt: String): String {
        return AppSettings2.instance.getActiveLLMClient().generateCommitMessage(prompt)
    }

    suspend fun refreshOpenAIModelIds() {
        AppSettings2.instance.getActiveLLMClient().refreshModels()
    }

    @Throws(Exception::class)
    suspend fun verifyOpenAIConfiguration(host: String, proxy: String?, timeout: String, token: String){
        AppSettings2.instance.getActiveLLMClient().verifyConfiguration(host, proxy, timeout, token)
    }
}
