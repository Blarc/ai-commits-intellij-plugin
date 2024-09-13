package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.commonBranch
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.isAmendCommitMode
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import git4idea.GitCommit
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LLMClientService<C : LLMClientConfiguration>(private val cs: CoroutineScope) {

    abstract suspend fun buildChatModel(client: C): ChatLanguageModel

    fun generateCommitMessage(clientConfiguration: C, commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, commitMessage: CommitMessage, project: Project) {

        val commitContext = commitWorkflowHandler.workflow.commitContext
        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges().toMutableList()

        cs.launch(ModalityState.current().asContextElement()) {
            withBackgroundProgress(project, message("action.background")) {

                if (commitContext.isAmendCommitMode) {
                    includedChanges += getLastCommitChanges(project)
                }

                val diff = computeDiff(includedChanges, false, project)
                if (diff.isBlank()) {
                    withContext(Dispatchers.EDT) {
                        sendNotification(Notification.emptyDiff())
                    }
                    return@withBackgroundProgress
                }

                val branch = commonBranch(includedChanges, project)
                val prompt = constructPrompt(AppSettings2.instance.activePrompt.content, diff, branch, commitMessage.text, project)

                sendRequest(clientConfiguration, prompt, onSuccess = {
                    withContext(Dispatchers.EDT) {
                        commitMessage.setCommitMessage(it)
                    }
                    AppSettings2.instance.recordHit()
                }, onError = {
                    withContext(Dispatchers.EDT) {
                        commitMessage.setCommitMessage(it)
                    }
                })
            }
        }
    }

    fun verifyConfiguration(client: C, label: JBLabel) {
        label.text = message("settings.verify.running")
        cs.launch(ModalityState.current().asContextElement()) {
            sendRequest(client, "test", onSuccess = {
                withContext(Dispatchers.EDT) {
                    label.text = message("settings.verify.valid")
                    label.icon = AllIcons.General.InspectionsOK
                }
            }, onError = {
                withContext(Dispatchers.EDT) {
                    label.text = it.wrap(60)
                    label.icon = AllIcons.General.InspectionsError
                }
            })
        }
    }

    private suspend fun sendRequest(client: C, text: String, onSuccess: suspend (r: String) -> Unit, onError: suspend (r: String) -> Unit) {
        try {
            val model = buildChatModel(client)
            val response = withContext(Dispatchers.IO) {
                model.generate(
                    listOf(
                        UserMessage.from(
                            "user",
                            text
                        )
                    )
                ).content().text()
            }
            onSuccess(response)
        } catch (e: IllegalArgumentException) {
            onError(message("settings.verify.invalid", e.message ?: message("unknown-error")))
        } catch (e: Exception) {
            onError(e.message ?: message("unknown-error"))
            // Generic exceptions should be logged by the IDE for easier error reporting
            throw e
        }
    }

    private suspend fun getLastCommitChanges(project: Project): List<Change> {
        return withContext(Dispatchers.IO) {
            GitRepositoryManager.getInstance(project).repositories.map { repo ->
                GitHistoryUtils.history(project, repo.root, "--max-count=1")
            }.filter { commits ->
                commits.isNotEmpty()
            }.map { commits ->
                (commits.first() as GitCommit).changes
            }.flatten()
        }
    }
}
