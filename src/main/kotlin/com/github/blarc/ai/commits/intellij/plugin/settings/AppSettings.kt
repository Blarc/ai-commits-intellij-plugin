package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.prompt.Prompt
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
import java.util.*

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
    var proxyUrl: String? = null

    var prompts = initPrompts()
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

    fun getPrompt(diff: String, branch: String): String {
        var content = currentPrompt.content
        content = content.replace("{locale}", locale.displayLanguage)
        content = content.replace("{branch}", branch)

        return if (content.contains("{diff}")) {
            content.replace("{diff}", diff)
        } else {
            "$content\n$diff"
        }
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
                proxy = proxyUrl?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) }
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

    private fun initPrompts() = mutableMapOf(
            // Generate UUIDs for game objects in Mine.py and call the function in start_game().
            "basic" to Prompt("Basic",
                    "Basic prompt that generates a decent commit message.",
                    "Write an insightful but concise Git commit message in a complete sentence in present tense for the " +
                            "following diff without prefacing it with anything, the response must be in the language {locale} and must " +
                            "NOT be longer than 74 characters. The sent text will be the differences between files, where deleted lines" +
                            " are prefixed with a single minus sign and added lines are prefixed with a single plus sign.\n" +
                            "{diff}",
                    false),
            // feat: generate unique UUIDs for game objects on Mine game start
            "conventional" to Prompt("Conventional",
                    "Prompt for commit message in the conventional commit convention.",
                    "Write a clean and comprehensive commit message in the conventional commit convention. " +
                            "I'll send you an output of 'git diff --staged' command, and you convert " +
                            "it into a commit message. " +
                            "Do NOT preface the commit with anything. " +
                            "Do NOT add any descriptions to the commit, only commit message. " +
                            "Use the present tense. " +
                            "Lines must not be longer than 74 characters. " +
                            "Use {locale} language to answer.\n" +
                            "{diff}",
                    false),
            // ✨ feat(mine): Generate objects UUIDs and start team timers on game start
            "emoji" to Prompt("Emoji",
                    "Prompt for commit message in the conventional commit convention with GitMoji convention.",
                    "Write a clean and comprehensive commit message in the conventional commit convention. " +
                            "I'll send you an output of 'git diff --staged' command, and you convert " +
                            "it into a commit message. " +
                            "Use GitMoji convention to preface the commit. " +
                            "Do NOT add any descriptions to the commit, only commit message. " +
                            "Use the present tense. " +
                            "Lines must not be longer than 74 characters. " +
                            "Use {locale} language to answer.\n" +
                            "{diff}",
                    false)
    )

    class LocaleConverter : Converter<Locale>() {
        override fun toString(value: Locale): String? {
            return value.toLanguageTag()
        }

        override fun fromString(value: String): Locale? {
            return Locale.forLanguageTag(value)
        }
    }
}
