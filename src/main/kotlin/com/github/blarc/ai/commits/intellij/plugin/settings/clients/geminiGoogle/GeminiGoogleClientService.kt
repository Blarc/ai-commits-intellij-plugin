
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiGoogle.GeminiGoogleClientConfiguration
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class GeminiGoogleClientService(private val cs: CoroutineScope) : LLMClientService<GeminiGoogleClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiGoogleClientService = service()
    }

    override suspend fun buildChatModel(client: GeminiGoogleClientConfiguration): ChatLanguageModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        return GoogleAiGeminiChatModel.builder()
            .apiKey(token)
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .topK(40)
            .build()
    }

    override suspend fun buildStreamingChatModel(client: GeminiGoogleClientConfiguration) = null

    fun saveToken(client: GeminiGoogleClientConfiguration, token: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id), token)
                client.tokenIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }
}
