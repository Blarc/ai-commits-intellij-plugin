package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCommonBranch
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral.MistralAIClientSharedState
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import com.intellij.openapi.vcs.changes.Change
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.isAmendCommitMode
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import git4idea.GitCommit
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.*
import javax.swing.DefaultComboBoxModel

abstract class LLMClientService<C : LLMClientConfiguration>(private val cs: CoroutineScope)
    : LLMServiceBase<C>(cs) {

    // This function should be implemented only by LLM services that can refresh models via API
    open suspend fun getAvailableModels(client: C): List<String>  {
        return listOf()
    }

    abstract suspend fun buildChatModel(client: C): ChatModel

    abstract suspend fun buildStreamingChatModel(client: C): StreamingChatModel?

    fun refreshModels(client: C, comboBox: ComboBox<String>, label: JBLabel) {
        label.text = message("settings.refreshModels.running")
        label.icon = AllIcons.General.InlineRefresh
        cs.launch(ModalityState.current().asContextElement()) {
            makeRequestWithTryCatch(function = {
                val availableModels = getAvailableModels(client);

                MistralAIClientSharedState.getInstance().modelIds.addAll(availableModels)

                withContext(Dispatchers.EDT) {
                    label.text = message("settings.refreshModels.success")
                    label.icon = AllIcons.General.InspectionsOK

                    // This can't be called from EDT thread, because dialog blocks the EDT thread
                    val modelItems = client.getModelIds().naturalSorted().toTypedArray()
                    comboBox.model = DefaultComboBoxModel(modelItems)
                    comboBox.item = client.modelId
                }
            }, onError = {
                withContext(Dispatchers.EDT) {
                    label.text = it.wrap(60)
                    label.icon = AllIcons.General.InspectionsError
                }
            })
        }
    }

    override fun generateCommitMessage(clientConfiguration: C, commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        generateCommitMessageJob = cs.launch(ModalityState.current().asContextElement()) {
            withBackgroundProgress(project, message("action.background")) {
                val (_, prompt) = prepareCommitMessageRequest(
                    commitWorkflowHandler, project
                ) ?: return@withBackgroundProgress

                makeRequest(clientConfiguration, prompt, onSuccess = {
                    withContext(Dispatchers.EDT) {
                        clientConfiguration.setCommitMessage(commitWorkflowHandler, prompt, it)
                    }
                    AppSettings2.instance.recordHit()
                }, onError = {
                    withContext(Dispatchers.EDT) {
                        commitWorkflowHandler.setCommitMessage(it)
                    }
                })
            }
        }
    }

    fun verifyConfiguration(client: C, label: JBLabel) {
        label.text = message("settings.verify.running")
        label.icon = AllIcons.General.InlineRefresh
        cs.launch(ModalityState.current().asContextElement()) {
            makeRequest(client, "test", onSuccess = {
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

    private suspend fun makeRequestWithTryCatch(function: suspend () -> Unit, onError: suspend (r: String) -> Unit) {
        try {
            function()
        } catch (e: IllegalArgumentException) {
            onError(message("settings.verify.invalid", e.message ?: message("unknown-error")))
        } catch (e: Exception) {
            onError(e.message ?: message("unknown-error"))
            // Generic exceptions should be logged by the IDE for easier error reporting
            throw e
        }
    }

    private suspend fun makeRequest(client: C, text: String, onSuccess: suspend (r: String) -> Unit, onError: suspend (r: String) -> Unit) {
        makeRequestWithTryCatch(function = {
            if (AppSettings2.instance.useStreamingResponse) {
                buildStreamingChatModel(client)?.let { streamingChatModel ->
                    sendStreamingRequest(streamingChatModel, text, onSuccess)
                    return@makeRequestWithTryCatch
                }
            }
            sendRequest(client, text, onSuccess)
        }, onError = onError)
    }

    private suspend fun sendStreamingRequest(streamingModel: StreamingChatModel, text: String, onSuccess: suspend (r: String) -> Unit) {
        var response = ""
        val completionDeferred = CompletableDeferred<String>()

        withContext(Dispatchers.IO) {
            streamingModel.chat(
                listOf(
                    UserMessage.from(
                        "user",
                        text
                    )
                ),
                object : StreamingChatResponseHandler {
                    override fun onPartialResponse(partialResponse: String?) {
                        response += partialResponse
                        cs.launch {
                            onSuccess(response)
                        }
                    }

                    override fun onCompleteResponse(completeResponse: ChatResponse) {
                        completionDeferred.complete(completeResponse.aiMessage().text())
                    }

                    override fun onError(error: Throwable) {
                        completionDeferred.completeExceptionally(error)
                    }
                }
            )
            // This throws exception if completionDeferred.completeExceptionally(error) is called
            // which is handled by the function calling this function
            onSuccess(completionDeferred.await())
        }
    }

    private suspend fun sendRequest(client: C, text: String, onSuccess: suspend (r: String) -> Unit) {
        val model = buildChatModel(client)
        val response = withContext(Dispatchers.IO) {
            model.chat(
                listOf(
                    UserMessage.from(
                        "user",
                        text
                    )
                )
            ).aiMessage().text()
        }
        onSuccess(response)
    }
}
