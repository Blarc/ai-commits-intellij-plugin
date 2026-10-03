package com.github.blarc.ai.commits.intellij.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommitMessageCleanupTest {

    @Test
    fun stripsThinkBlocksBeforeCleanupRegex() {
        assertEquals(
            "feat: add",
            AICommitsUtils.cleanCommitMessage("<think>secret</think>\n\nfeat: add", Regex(""))
        )
        assertEquals(
            "visible",
            AICommitsUtils.cleanCommitMessage(
                "<think type=\"reasoning\">feat: hidden</think>\nfeat: visible",
                Regex("^feat:\\s+")
            )
        )
        assertEquals(
            "feat: real",
            AICommitsUtils.cleanCommitMessage("feat: real\n<think>still open", Regex(""))
        )
        assertEquals(
            "<thinking>keep</thinking>",
            AICommitsUtils.cleanCommitMessage("<thinking>keep</thinking>", Regex(""))
        )
        assertEquals(
            "FEAT: visible",
            AICommitsUtils.cleanCommitMessage("<think>secret</think>\nFEAT: visible", Regex("^feat:\\s+"))
        )
    }
}
