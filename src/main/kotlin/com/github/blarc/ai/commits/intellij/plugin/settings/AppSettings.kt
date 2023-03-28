package com.github.blarc.ai.commits.intellij.plugin.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.Locale

@State(
    name = AppSettings.SERVICE_NAME,
    storages = [Storage("AICommit.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings> {

    private val openAITokenTitle = "OpenAIToken"
    var locale: Locale = Locale.getDefault()
    var requestSupport = true
    companion object {
        const val SERVICE_NAME = "com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings"

        val instance: AppSettings
            get() = ApplicationManager.getApplication().getService(AppSettings::class.java)
    }

    fun saveOpenAIToken(token: String) {
        PasswordSafe.instance.setPassword(getCredentialAttributes(openAITokenTitle), token)
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

}