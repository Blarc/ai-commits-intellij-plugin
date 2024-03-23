package com.github.blarc.ai.commits.intellij.plugin.settings.clients

class TestAIClient(displayName: String = "TestAI") : LLMClient(
    displayName,
    "testAiBaseUrl",
    mutableSetOf("testAiBaseUrl"),
    null,
    30,
    "gpt-3.5-turbo",
    listOf("gpt-3.5-turbo", "gpt-4"),
    "0.7"
) {
    override suspend fun generateCommitMessage(prompt: String): String {
        return "hello world"
    }

    override suspend fun refreshModels() {
    }

    override suspend fun verifyConfiguration(newHost: String, newProxy: String?, newTimeout: String, newToken: String) {
    }

}
