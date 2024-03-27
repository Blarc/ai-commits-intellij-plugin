package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import javax.swing.JComponent

interface LLMClientPanel {

    fun create(): JComponent

}
