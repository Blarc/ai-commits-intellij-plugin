package com.github.blarc.ai.commits.intellij.plugin.settings.clients.gigachat

import chat.giga.model.Scope
import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import javax.swing.Icon

class GigachatClientConfiguration : LLMClientConfiguration (
    "Gigachat",
    "GigaChat-2-Max",
    "0.7"
)  {

    @Attribute
    var apiUrl: String = "https://gigachat.devices.sberbank.ru/api/v1"
    @Attribute
    var authUrl: String = "https://ngw.devices.sberbank.ru:9443/api/v2"
    @Attribute
    var timeout: Int = 60
    @Attribute
    var tokenIsStored: Boolean = false
    @Transient
    var token: String? = null

    @OptionTag(converter = ScopeConverter::class)
    var scope: Scope? = null

    companion object {
        const val CLIENT_NAME = "Gigachat"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GIGACHAT.getThemeBasedIcon()
    }

    override fun getSharedState(): GigachatClientSharedState {
        return GigachatClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return GigachatClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return GigachatClientService.getInstance().generateCommitMessageJob
    }

    fun getAuthUrls(): Set<String> {
        return getSharedState().authUrls
    }

    fun addAuthUrl(authUrl: String) {
        getSharedState().authUrls.add(authUrl)
    }

    override fun clone(): LLMClientConfiguration {
        val copy = GigachatClientConfiguration()
        copy.id = id
        copy.name = name
        copy.tokenIsStored = tokenIsStored
        copy.apiUrl = apiUrl
        copy.authUrl = authUrl
        copy.scope = scope
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
    }

    override fun panel() = GigachatClientPanel(this)


    class ScopeConverter : Converter<Scope>() {
        override fun toString(value: Scope): String? {
            return value.toString()
        }

        override fun fromString(value: String): Scope? {
            return Scope.values().find { it.name == value }
        }
    }
}