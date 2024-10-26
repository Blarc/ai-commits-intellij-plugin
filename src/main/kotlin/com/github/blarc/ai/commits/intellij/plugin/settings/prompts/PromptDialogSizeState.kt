package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(name = "PromptDialogSettings", storages = [Storage("AICommitsPromptDialogSettings.xml")])
class PromptDialogSizeState : PersistentStateComponent<PromptDialogSizeState> {
    var width: Int = 800
    var height: Int = 600

    override fun getState(): PromptDialogSizeState? = this

    override fun loadState(state: PromptDialogSizeState) {
        this.width = state.width
        this.height = state.height
    }

    companion object {
        @JvmStatic
        fun getInstance(): PromptDialogSizeState = service()
    }
}