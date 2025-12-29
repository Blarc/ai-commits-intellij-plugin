package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiGoogle

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GeminiApiClientSharedState", storages = [Storage("AICommitsGeminiApi.xml")])
class GeminiGoogleClientSharedState : PersistentStateComponent<GeminiGoogleClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiGoogleClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gemini-2.5-pro",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.5-flash-preview-native-audio-dialog",
        "gemini-2.5-flash-exp-native-audio-thinking-dialog",
        "gemini-2.5-flash-image-preview",
        "gemini-2.5-flash-preview-tts",
        "gemini-2.5-pro-preview-tts",
        "gemini-2.0-flash",
        "gemini-2.0-flash-preview-image-generation",
        "gemini-2.0-flash-lite",
        "gemini-2.0-flash-live-001"
    )

    override fun getState(): GeminiGoogleClientSharedState = this

    override fun loadState(state: GeminiGoogleClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
