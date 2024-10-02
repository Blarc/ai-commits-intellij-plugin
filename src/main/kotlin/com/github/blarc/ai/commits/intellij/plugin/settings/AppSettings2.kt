package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic.AnthropicClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.azureOpenAi.AzureOpenAiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.gemini.GeminiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.huggingface.HuggingFaceClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama.OllamaClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientSharedState
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan.QianfanClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.DefaultPrompts
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
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
            QianfanClientConfiguration::class,
            GeminiClientConfiguration::class,
            AnthropicClientConfiguration::class,
            AzureOpenAiClientConfiguration::class,
            HuggingFaceClientConfiguration::class
        ],
        style = XCollection.Style.v2
    )
    var llmClientConfigurations = setOf<LLMClientConfiguration>(
        OpenAiClientConfiguration()
    )

    @Attribute
    var activeLlmClientId: String? = null

    @XMap
    var prompts = DefaultPrompts.toPromptsMap()
    var activePrompt = DefaultPrompts.BASIC.prompt

    var appExclusions: Set<String> = setOf()

    override fun getState() = this

    override fun loadState(state: AppSettings2) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun noStateLoaded() {
        val appSettings = AppSettings.instance
        migrateSettingsFromVersion1(appSettings)
        val openAiLlmClient = llmClientConfigurations.find { it.getClientName() == OpenAiClientConfiguration.CLIENT_NAME }
        migrateOpenAiClientFromVersion1(openAiLlmClient as OpenAiClientConfiguration, appSettings)
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

    private fun migrateOpenAiClientFromVersion1(openAiLlmClientConfiguration: OpenAiClientConfiguration?, appSettings: AppSettings) {
        openAiLlmClientConfiguration?.apply {
            host = appSettings.openAIHost
            appSettings.openAISocketTimeout.toIntOrNull()?.let { timeout = it }
            proxyUrl = appSettings.proxyUrl
            modelId = appSettings.openAIModelId
            temperature = appSettings.openAITemperature

            val credentialAttributes = getCredentialAttributes(appSettings.openAITokenTitle)
            migrateToken(credentialAttributes)
        }

        OpenAiClientSharedState.getInstance().hosts.addAll(appSettings.openAIHosts)
        OpenAiClientSharedState.getInstance().modelIds.addAll(appSettings.openAIModelIds)
    }

    private fun OpenAiClientConfiguration.migrateToken(credentialAttributes: CredentialAttributes) {
        PasswordSafe.instance.getAsync(credentialAttributes)
            .onSuccess {
                it?.password?.let { token ->
                    try {
                        PasswordSafe.instance.setPassword(getCredentialAttributes(id), token.toString(false))
                    } catch (e: Exception) {
                        sendNotification(Notification.unableToSaveToken(e.message))
                    }
                    tokenIsStored = true
                }
            }
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

    fun getActiveLLMClientConfiguration(): LLMClientConfiguration? {
        return getActiveLLMClientConfiguration(activeLlmClientId)
    }

    fun getActiveLLMClientConfiguration(activeLLMClientConfigurationId: String?): LLMClientConfiguration? {
        return llmClientConfigurations.find { it.id == activeLLMClientConfigurationId }
            ?: llmClientConfigurations.firstOrNull()
    }

    fun setActiveLlmClient(newId: String) {
        // TODO @Blarc: Throw exception if llm client id is not valid
        llmClientConfigurations.find { it.id == newId }?.let {
            activeLlmClientId = newId
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
