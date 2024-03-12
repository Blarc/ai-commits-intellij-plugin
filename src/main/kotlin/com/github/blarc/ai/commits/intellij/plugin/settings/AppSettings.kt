package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.DefaultPrompts
import com.github.blarc.ai.commits.intellij.plugin.settings.providers.OpenAIClient
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

@State(
    name = AppSettings.SERVICE_NAME,
    storages = [
        Storage("AICommit.xml", deprecated = true),
        Storage("AICommits2.xml")
    ]
)
class AppSettings : PersistentStateComponent<AppSettings> {

    private var hits = 0
    var requestSupport = true
    var lastVersion: String? = null

    @OptionTag(converter = LocaleConverter::class)
    var locale: Locale = Locale.ENGLISH

    var llmProviders = setOf(OpenAIClient.instance)
    var currentLlmProvider = OpenAIClient.instance

    var prompts = DefaultPrompts.toPromptsMap()
    var currentPrompt = prompts["basic"]!!

    var appExclusions: Set<String> = setOf()

    // Old single LLM provider configuration - needed for migration
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAIHost = OpenAIHost.OpenAI.baseUrl
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAIHosts = mutableSetOf(OpenAIHost.OpenAI.baseUrl)
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAISocketTimeout = "30"
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var proxyUrl: String? = null
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAIModelId = "gpt-3.5-turbo"
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAIModelIds = listOf("gpt-3.5-turbo", "gpt-4")
    @Deprecated("Old configuration property that is no longer used. Needed only for migrating.")
    var openAITemperature = "0.7"

    override fun getState() = this

    override fun loadState(state: AppSettings) {
        XmlSerializerUtil.copyBean(state, this)

        // Migration from single LLM provider to multiple LLM providers
        OpenAIClient.instance.host = openAIHost
        OpenAIClient.instance.hosts = openAIHosts
        openAISocketTimeout.toIntOrNull()?.let { OpenAIClient.instance.timeout = it }
        proxyUrl?.let { OpenAIClient.instance.proxyUrl = it }
        OpenAIClient.instance.modelId = openAIModelId
        OpenAIClient.instance.modelIds = openAIModelIds
        OpenAIClient.instance.temperature = openAITemperature
        AICommitsUtils.retrieveToken( "OpenAIToken")?.let { OpenAIClient.instance.token = it }
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

    companion object {
        const val SERVICE_NAME = "com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings"
        val instance: AppSettings
            get() = ApplicationManager.getApplication().getService(AppSettings::class.java)
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
