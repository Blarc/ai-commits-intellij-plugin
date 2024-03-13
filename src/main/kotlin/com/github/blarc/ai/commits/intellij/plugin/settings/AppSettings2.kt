package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.DefaultPrompts
import com.github.blarc.ai.commits.intellij.plugin.settings.providers.LLMClient
import com.github.blarc.ai.commits.intellij.plugin.settings.providers.OpenAIClient
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

@State(
    name = AppSettings2.SERVICE_NAME,
    storages = [
        Storage("AICommits2.xml")
    ]
)
@Service(Service.Level.APP)
class AppSettings2 : PersistentStateComponent<AppSettings2> {

    companion object {
        const val SERVICE_NAME = "com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2"
        val instance: AppSettings2
            get() = ApplicationManager.getApplication().getService(AppSettings2::class.java)
    }

    private var hits = 0
    var requestSupport = true
    var lastVersion: String? = null

    @OptionTag(converter = LocaleConverter::class)
    var locale: Locale = Locale.ENGLISH

    private var activeLlmClient = "OpenAI"
    var llmClients = mapOf(
        "OpenAI" to OpenAIClient()
    )

    var prompts = DefaultPrompts.toPromptsMap()
    var currentPrompt = prompts["basic"]!!

    var appExclusions: Set<String> = setOf()

    override fun getState() = this

    override fun loadState(state: AppSettings2) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun noStateLoaded() {
        val appSettings = AppSettings.instance

        // Migration from old settings
        hits = appSettings.hits
        locale = appSettings.locale
        lastVersion = appSettings.lastVersion
        requestSupport = appSettings.requestSupport

        prompts = appSettings.prompts
        currentPrompt = appSettings.currentPrompt

        appExclusions = appSettings.appExclusions

        // TODO: currentLlmClient vs OpenAIClient.instance()
        llmClients["OpenAI"]?.host = appSettings.openAIHost
        llmClients["OpenAI"]?.hosts = appSettings.openAIHosts
        appSettings.openAISocketTimeout.toIntOrNull()?.let { llmClients["OpenAI"]?.timeout = it }
        appSettings.proxyUrl?.let { llmClients["OpenAI"]?.proxyUrl = it }
        llmClients["OpenAI"]?.modelId = appSettings.openAIModelId
        llmClients["OpenAI"]?.modelIds = appSettings.openAIModelIds
        llmClients["OpenAI"]?.temperature = appSettings.openAITemperature
        AICommitsUtils.retrieveToken( appSettings.openAITokenTitle)?.let { llmClients["OpenAI"]?.token = it }
    }

    fun recordHit() {
        hits++
        if (requestSupport && (hits == 50 || hits % 100 == 0)) {
            sendNotification(Notification.star())
        }
    }

    fun isPathExcluded(path: String): Boolean {
        return AICommitsUtils.matchesGlobs(path, appExclusions)
    }

    fun getActiveLLMClient(): LLMClient {
        return llmClients[activeLlmClient]!!
    }

    fun setActiveLlmClient(llmClientName: String) {
        // TODO @Blarc: Throw exception if llm client name is not valid
        llmClients[llmClientName]?.let {
            activeLlmClient = llmClientName
        }
    }

    class LocaleConverter : Converter<Locale>() {
        override fun toString(value: Locale): String? {
            return value.toLanguageTag()
        }

        override fun fromString(value: String): Locale? {
            return Locale.forLanguageTag(value)
        }
    }
}
