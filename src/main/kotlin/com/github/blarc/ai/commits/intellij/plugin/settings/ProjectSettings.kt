package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute

@State(
        name = ProjectSettings.SERVICE_NAME,
        storages = [Storage("AICommit.xml")]
)
@Service(Service.Level.PROJECT)
class ProjectSettings : PersistentStateComponent<ProjectSettings?> {

    companion object {
        const val SERVICE_NAME = "com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings"
    }

    var projectExclusions: Set<String> = setOf()

    @Attribute
    var activeLlmClientId: String? = null
    @Attribute
    var isProjectSpecificLLMClient: Boolean = false

    override fun getState() = this

    override fun loadState(state: ProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun isPathExcluded(path: String): Boolean {
        return AICommitsUtils.matchesGlobs(path, projectExclusions)
    }

    fun getActiveLLMClientConfiguration(): LLMClientConfiguration? {
        return AppSettings2.instance.getActiveLLMClientConfiguration(activeLlmClientId)
    }

}
