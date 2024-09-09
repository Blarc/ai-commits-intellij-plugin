package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.components.JBLabel
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LLMClientService<T : LLMClientConfiguration>(private val cs: CoroutineScope) {

    abstract suspend fun buildChatModel(client: T): ChatLanguageModel

    fun generateCommitMessage(client: T, prompt: String, project: Project, commitMessage: CommitMessage) {
        cs.launch(Dispatchers.IO + ModalityState.current().asContextElement()) {
            withBackgroundProgress(project, message("action.background")) {
                sendRequest(client, prompt, onSuccess = {
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

    fun verifyConfiguration(client: T, label: JBLabel) {
        label.text = message("settings.verify.running")
        cs.launch(Dispatchers.IO + ModalityState.current().asContextElement()) {
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

    private suspend fun sendRequest(client: T, text: String, onSuccess: suspend (r: String) -> Unit, onError: suspend (r: String) -> Unit) {
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
}
