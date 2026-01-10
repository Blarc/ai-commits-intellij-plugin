package com.github.blarc.ai.commits.intellij.plugin.settings.clients

interface LlmClientSharedState {

    val hosts: MutableSet<String>

    val modelIds: MutableSet<String>
}
