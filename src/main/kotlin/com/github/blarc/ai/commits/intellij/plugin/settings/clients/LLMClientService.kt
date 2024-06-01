package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.ui.components.JBLabel
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LLMClientService<T : LLMClientConfiguration>(private val cs: CoroutineScope) {

    abstract suspend fun buildChatModel(client: T): ChatLanguageModel

    fun generateCommitMessage(client: T, prompt: String, commitMessage: CommitMessage) {
        cs.launch(Dispatchers.Default) {
            sendRequest(client, prompt, onSuccess = {
                commitMessage.setCommitMessage(it)
                AppSettings2.instance.recordHit()
            }, onError = {
                commitMessage.setCommitMessage(it)
            })
        }
    }

    fun verifyConfiguration(client: T, label: JBLabel) {
        // TODO @Blarc: Can you make this better?
        label.text = "Verifying configuration..."
        cs.launch(Dispatchers.Default) {
            sendRequest(client, "test", onSuccess = {
                label.text = message("settings.verify.valid")
                label.icon = AllIcons.General.InspectionsOK
            }, onError = {
                label.text = it.wrap(80)
                label.icon = AllIcons.General.InspectionsError
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
            onError("Invalid configuration: ${e.message ?: "unknown"}.")
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error.")
        }
    }
}
