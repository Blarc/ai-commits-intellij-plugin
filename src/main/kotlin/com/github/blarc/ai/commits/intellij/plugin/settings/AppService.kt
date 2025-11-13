import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientConfiguration
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.OneTimeString
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.APP)
class AppService(private val cs: CoroutineScope) {

    companion object {
        val instance: AppService
            get() = ApplicationManager.getApplication().getService(AppService::class.java)
    }

    fun retrieveToken(
        credentialAttributes: CredentialAttributes,
        onToken: suspend (OneTimeString?) -> Unit,
    ) {
        cs.launch {
            withContext(Dispatchers.IO) {
                PasswordSafe.instance.get(credentialAttributes)?.password?.let { token ->
                    onToken(token)
                }
            }
        }
    }
}