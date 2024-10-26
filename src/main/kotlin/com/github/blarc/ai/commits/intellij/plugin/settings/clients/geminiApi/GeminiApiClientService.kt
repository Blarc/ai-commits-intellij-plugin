import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiApi.GeminiApiClientConfiguration
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.APP)
class GeminiApiClientService(private val cs: CoroutineScope) : LLMClientService<GeminiApiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiApiClientService = service()
    }

    override suspend fun buildChatModel(client: GeminiApiClientConfiguration): ChatLanguageModel {
        return GoogleAiGeminiChatModel.builder()
            .apiKey(client.apiKey)
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .build()
    }

    override suspend fun buildStreamingChatModel(client: GeminiApiClientConfiguration) = null
}
