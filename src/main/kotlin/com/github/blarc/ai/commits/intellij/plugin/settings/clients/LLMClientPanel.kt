package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.intellij.openapi.ui.DialogPanel

interface LLMClientPanel {

    fun create(): DialogPanel

}
