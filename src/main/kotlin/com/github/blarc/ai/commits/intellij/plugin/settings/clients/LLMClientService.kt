package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.ui.components.JBLabel
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LLMClientService <T: LLMClientConfiguration>(private val cs: CoroutineScope) {

    abstract fun buildChatModel(client: T): ChatLanguageModel

    fun generateCommitMessage(client: T, prompt: String, commitMessage: CommitMessage) {
        // TODO @Blarc: catch exceptions
        val model = buildChatModel(client)
        sendRequest(model, prompt) {
            withContext(Dispatchers.EDT) {
                commitMessage.setCommitMessage(it.content().text())
            }
            AppSettings2.instance.recordHit()
        }
    }

    fun verifyConfiguration(client: T, label: JBLabel) {
        // TODO @Blarc: catch exceptions
        val model = buildChatModel(client)
        sendRequest(model, "test") {
            withContext(Dispatchers.EDT) {
                label.text = message("settings.verify.valid")
                label.icon = AllIcons.General.InspectionsOK
            }
        }

//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                client.verifyConfiguration(hostComboBox.item, proxyTextField.text, socketTimeoutTextField.text, modelComboBox.item, String(tokenPasswordField.password))
//                verifyLabel.text = message("settings.verify.valid")
//                verifyLabel.icon = AllIcons.General.InspectionsOK
//            } catch (e: Exception) {
//                var errorMessage = e.localizedMessage
//                if (e.cause is OpenAiHttpException) {
//                    val openAiError = Json.decodeFromString<OpenAiErrorWrapper>((e.cause as OpenAiHttpException).message!!).error
//                    errorMessage = openAiError.code
//                }
//                verifyLabel.text = message("settings.verify.invalid", errorMessage)
//                verifyLabel.icon = AllIcons.General.InspectionsError
//            }
//        }
    }

//    @Serializable
//    data class OpenAiError(val message: String?, val type: String?, val param: String?, val code: String?)
//
//    @Serializable
//    data class OpenAiErrorWrapper(val error: OpenAiError)

    private fun sendRequest(model: ChatLanguageModel, text: String, onResponse: suspend (r: Response<AiMessage>) -> Unit ) {
        cs.launch(Dispatchers.Default) {
            val response = withContext(Dispatchers.IO) {
                model.generate(
                    listOf(
                        UserMessage.from(
                            "user",
                            text
                        )
                    )
                )
            }
            onResponse(response)
        }
    }

}
