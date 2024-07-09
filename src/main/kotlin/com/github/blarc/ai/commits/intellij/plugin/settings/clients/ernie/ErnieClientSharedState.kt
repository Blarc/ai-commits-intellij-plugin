package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ernie

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.qianfan.QianfanChatModelNameEnum

@Service(Service.Level.APP)
@State(name = "ErnieClientSharedState", storages = [Storage("AICommitsOpenAi.xml")])
class ErnieClientSharedState : PersistentStateComponent<ErnieClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): ErnieClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://aip.baidubce.com")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds = QianfanChatModelNameEnum.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): ErnieClientSharedState = this

    override fun loadState(state: ErnieClientSharedState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
