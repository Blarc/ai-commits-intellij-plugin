package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "OpenAiClientService", storages = [Storage("AICommitsOllama.xml")])
class OllamaClientService : PersistentStateComponent<OllamaClientService> {
    @XCollection(style = XCollection.Style.v2)
    val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    val modelIds: MutableSet<String> = mutableSetOf("llama3")

    override fun getState(): OllamaClientService = this

    override fun loadState(state: OllamaClientService) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
