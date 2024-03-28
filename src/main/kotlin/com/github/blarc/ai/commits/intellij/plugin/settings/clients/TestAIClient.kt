package com.github.blarc.ai.commits.intellij.plugin.settings.clients

class TestAIClient(displayName: String = "TestAI") : LLMClient(
    displayName,
    "testAiBaseUrl",
    null,
    30,
    "gpt-3.5-turbo",
    "0.7"
) {
    override fun getHosts(): Set<String> {
        return mutableSetOf()
    }

    override fun getModelIds(): Set<String> {
        return mutableSetOf()
    }

    override suspend fun generateCommitMessage(prompt: String): String {
        return "hello world"
    }

    override suspend fun refreshModels() {
    }

    override suspend fun verifyConfiguration(newHost: String, newProxy: String?, newTimeout: String, newToken: String) {
    }

    override fun panel(): LLMClientPanel {
        return TestAIClientPanel(this)
    }
}
