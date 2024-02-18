package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.prompt.DefaultPrompts
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.kotlin.idea.gradleTooling.get
import java.util.*
import kotlin.time.Duration.Companion.seconds

@State(
    name = AppSettings.SERVICE_NAME,
    storages = [Storage("AICommit.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings> {

    private val openAITokenTitle = "OpenAIToken"
    private var hits = 0

    @OptionTag(converter = LocaleConverter::class)
    var locale: Locale = Locale.ENGLISH

    var requestSupport = true
    var lastVersion: String? = null
    var openAIHost = OpenAIHost.OpenAI.baseUrl
    var openAIHosts = mutableSetOf(OpenAIHost.OpenAI.baseUrl)
    var openAISocketTimeout = "30"
    var proxyUrl: String? = null

    var prompts = DefaultPrompts.toPromptsMap()
    var currentPrompt = prompts["basic"]!!

    var openAIModelId = "gpt-3.5-turbo"
    var openAIModelIds = listOf("gpt-3.5-turbo", "gpt-4")
    var openAITemperature = "0.7"

    var appExclusions: Set<String> = setOf()

    companion object {
        const val SERVICE_NAME = "com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings"
        val instance: AppSettings
            get() = ApplicationManager.getApplication().getService(AppSettings::class.java)
    }

    fun saveOpenAIToken(token: String) {
        try {
            PasswordSafe.instance.setPassword(getCredentialAttributes(openAITokenTitle), token)
        } catch (e: Exception) {
            sendNotification(Notification.unableToSaveToken())
        }
    }

    fun getOpenAIConfig(): OpenAIConfig {
        val token = getOpenAIToken() ?: throw Exception("OpenAI Token is not set.")
        return OpenAIConfig(
            token,
            host = openAIHost.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
            proxy = proxyUrl?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) },
            timeout = Timeout(socket = openAISocketTimeout.toInt().seconds)
        )
    }

    fun getOpenAIToken(): String? {
        val credentialAttributes = getCredentialAttributes(openAITokenTitle)
        val credentials: Credentials = PasswordSafe.instance.get(credentialAttributes) ?: return null
        return credentials.getPasswordAsString()
    }

    private fun getCredentialAttributes(title: String): CredentialAttributes {
        return CredentialAttributes(
            title,
            null,
            this.javaClass,
            false
        )
    }

    override fun getState() = this

    override fun loadState(state: AppSettings) {
        XmlSerializerUtil.copyBean(state, this)
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

    class LocaleConverter : Converter<Locale>() {
        override fun toString(value: Locale): String? {
            return value.toLanguageTag()
        }

        override fun fromString(value: String): Locale? {
            return Locale.forLanguageTag(value)
        }
    }
}
