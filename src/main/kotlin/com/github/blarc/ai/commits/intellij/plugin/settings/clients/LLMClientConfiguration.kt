package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
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

abstract class LLMClientConfiguration(
    @Attribute var name: String,
    @Attribute var modelId: String
) : Cloneable, Comparable<LLMClientConfiguration>, AnAction() {

    @Attribute
    var id: String = UUID.randomUUID().toString()

    abstract fun getClientName(): String

    abstract fun getClientIcon(): Icon

    abstract fun getSharedState(): LLMClientSharedState

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

        generateCommitMessage(commitWorkflowHandler, project)
    }

    open fun setCommitMessage(
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        prompt: String,
        result: String
    ) {
        // Remove <think>content</think> style tags and their content
        val cleanedResult = result.replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "").trim()
        commitWorkflowHandler.setCommitMessage(cleanedResult)
    }

    abstract fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project)

    abstract fun getGenerateCommitMessageJob(): Job?

    public abstract override fun clone(): LLMClientConfiguration

    abstract fun panel(): LLMClientPanel

    override fun compareTo(other: LLMClientConfiguration): Int {
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
