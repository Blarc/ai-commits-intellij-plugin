package com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.qianfan.QianfanChatModelNameEnum

@Service(Service.Level.APP)
@State(name = "QianfanClientSharedState", storages = [Storage("AICommitsOpenAi.xml")])
class QianfanClientSharedState : PersistentStateComponent<QianfanClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): QianfanClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://aip.baidubce.com")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds = QianfanChatModelNameEnum.entries.stream()
        .map { it.modelName }
        .toList()
        .toMutableSet()

    override fun getState(): QianfanClientSharedState = this

    override fun loadState(state: QianfanClientSharedState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
