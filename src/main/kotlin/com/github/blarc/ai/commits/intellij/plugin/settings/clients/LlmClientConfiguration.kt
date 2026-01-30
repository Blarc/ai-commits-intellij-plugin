package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import java.util.*
import javax.swing.Icon

abstract class LlmClientConfiguration(
    @Attribute var name: String,
    @Attribute var modelId: String
) : Cloneable, Comparable<LlmClientConfiguration>, AnAction() {

    @Attribute
    var id: String = UUID.randomUUID().toString()

    @Attribute
    var cleanupRegex: String = ""

    @Attribute
    var cleanupRegexIgnoreCase: Boolean = false

    abstract fun getClientName(): String

    abstract fun getClientIcon(): Icon

    abstract fun getSharedState(): LlmClientSharedState

    fun getHosts(): Set<String> {
        return getSharedState().hosts
    }

    fun getModelIds(): Set<String> {
        return getSharedState().modelIds
    }

    fun addHost(host: String) {
        getSharedState().hosts.add(host)
    }

    fun addModelId(modelId: String) {
        getSharedState().modelIds.add(modelId)
    }

    fun getCleanUpRegex(): Regex {
        return if (cleanupRegexIgnoreCase) {
            Regex(cleanupRegex, RegexOption.IGNORE_CASE)
        } else {
            Regex(cleanupRegex)
        }
    }

    fun generateCommitMessageAction(e: AnActionEvent) {
        val project = e.project ?: return

        val generateCommitMessageJob = getGenerateCommitMessageJob()
        if (generateCommitMessageJob?.isActive == true) {
            generateCommitMessageJob.cancel()
            return
        }

        val commitWorkflowHandler =
            e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>?
        if (commitWorkflowHandler == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        // Remember which LLM client was used for the shortcut action
        val projectSettings = project.service<ProjectSettings>()
        projectSettings.splitButtonActionSelectedLLMClientId = this.id

        // Look up the current configuration by ID to ensure we use the latest settings
        // (AnAction instances may be cached by IntelliJ and hold stale values)
        val currentConfig = AppSettings2.instance.llmClientConfigurations.find { it.id == this.id } ?: this
        currentConfig.generateCommitMessage(commitWorkflowHandler, project)
    }

    open fun setCommitMessage(
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        prompt: String,
        result: String
    ) {
        val cleanedResult = result.replace(getCleanUpRegex(), "").trim()
        // Clear existing message first, then set the new one
        commitWorkflowHandler.setCommitMessage("")
        commitWorkflowHandler.setCommitMessage(cleanedResult)
    }

    abstract fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project)

    abstract fun getGenerateCommitMessageJob(): Job?

    public abstract override fun clone(): LlmClientConfiguration

    abstract fun panel(): LlmClientPanel

    override fun compareTo(other: LlmClientConfiguration): Int {
        return name.compareTo(other.name)
    }

    override fun actionPerformed(e: AnActionEvent) {
        generateCommitMessageAction(e)
    }

    override fun update(e: AnActionEvent) {

        if (getGenerateCommitMessageJob()?.isActive == true) {
            e.presentation.icon = Icons.Process.STOP.getThemeBasedIcon()
        } else {
            e.presentation.icon = getClientIcon()
            // e.presentation.text = message("action.tooltip", name)
            e.presentation.text = name
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    open fun afterSerialization() {
        // Allow overriding
    }

//    override fun equals(other: Any?): Boolean {
//        return other is LLMClientConfiguration && other.id == id
//    }
//
//    override fun hashCode(): Int {
//        return id.hashCode()
//    }
}
