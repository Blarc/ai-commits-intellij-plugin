package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama.OllamaClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientService
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.DefaultPrompts
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XCollection
import com.intellij.util.xmlb.annotations.XMap
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


    @XCollection(
        elementTypes = [
            OpenAiClientConfiguration::class,
            OllamaClientConfiguration::class,
        ],
        style = XCollection.Style.v2
    )
    var llmClientConfigurations = setOf<LLMClientConfiguration>(
        OpenAiClientConfiguration()
    )

    private var activeLlmClient = "OpenAI"

    @XMap
    var prompts = DefaultPrompts.toPromptsMap()
    var activePrompt = prompts["basic"]!!

    var appExclusions: Set<String> = setOf()

    override fun getState() = this

    override fun loadState(state: AppSettings2) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun noStateLoaded() {
        val appSettings = AppSettings.instance
        migrateSettingsFromVersion1(appSettings)
        val openAiLlmClient = llmClientConfigurations.find { it.displayName == "OpenAI" }
        migrateOpenAiClientFromVersion1(openAiLlmClient, appSettings)
    }

    private fun migrateSettingsFromVersion1(appSettings: AppSettings) {
        hits = appSettings.hits
        locale = appSettings.locale
        lastVersion = appSettings.lastVersion
        requestSupport = appSettings.requestSupport
        prompts = appSettings.prompts
        activePrompt = appSettings.currentPrompt
        appExclusions = appSettings.appExclusions
    }

    private fun migrateOpenAiClientFromVersion1(openAiLlmClientConfiguration: LLMClientConfiguration?, appSettings: AppSettings) {
        openAiLlmClientConfiguration?.apply {
            host = appSettings.openAIHost
            appSettings.openAISocketTimeout.toIntOrNull()?.let { timeout = it }
            proxyUrl = appSettings.proxyUrl
            modelId = appSettings.openAIModelId
            temperature = appSettings.openAITemperature
            AICommitsUtils.retrieveToken(appSettings.openAITokenTitle)?.let { token = it }
        }

        service<OpenAiClientService>().hosts.addAll(appSettings.openAIHosts)
        service<OpenAiClientService>().modelIds.addAll(appSettings.openAIModelIds)
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

    fun getActiveLLMClient(): LLMClientConfiguration {
        return llmClientConfigurations.find { it.displayName == activeLlmClient }!!
    }

    fun setActiveLlmClient(llmClientConfiguration: LLMClientConfiguration) {
        // TODO @Blarc: Throw exception if llm client name is not valid
        llmClientConfigurations.find { it.displayName == llmClientConfiguration.displayName }?.let {
            activeLlmClient = llmClientConfiguration.displayName
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
