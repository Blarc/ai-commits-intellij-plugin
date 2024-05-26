package com.github.blarc.ai.commits.intellij.plugin.settings.clients

interface LLMClientSharedState {

    val hosts: MutableSet<String>

    val modelIds: MutableSet<String>
}
