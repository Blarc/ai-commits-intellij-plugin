package com.github.blarc.ai.commits.intellij.plugin.settings.clients.amazonBedrock;

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.Job
import software.amazon.awssdk.regions.Region
import javax.swing.Icon

class AmazonBedrockClientConfiguration : LLMClientConfiguration(
    "Amazon Bedrock",
    "us.amazon.nova-lite-v1:0",
    "0.7"
) {

    @Attribute
    var accessKeyId: String? = null
    @Attribute
    var accessKeyIsStored: Boolean = false
    @Transient
    var accessKey: String? = null
    @Attribute
    var timeout: Int = 30
    @Attribute
    var topP: Double? = null
    @Attribute
    var topK: Int? = null
    @Attribute
    var maxOutputTokens: Int? = null
    @OptionTag(converter = RegionConverter::class)
    var region: Region = Region.EU_CENTRAL_1

    companion object {
        const val CLIENT_NAME = "Amazon Bedrock"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.AMAZON_BEDROCK.getThemeBasedIcon()
    }

    override fun getSharedState(): LLMClientSharedState {
        return AmazonBedrockClientSharedState.getInstance()
    }

    override fun generateCommitMessage(commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>, project: Project) {
        return AmazonBedrockClientService.getInstance().generateCommitMessage(this, commitWorkflowHandler, project)
    }

    override fun getGenerateCommitMessageJob(): Job? {
        return AmazonBedrockClientService.getInstance().generateCommitMessageJob
    }

    override fun clone(): LLMClientConfiguration {
        val copy = AmazonBedrockClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.accessKeyId = accessKeyId
        copy.accessKeyIsStored = accessKeyIsStored
        copy.accessKey = accessKey
        copy.timeout = timeout
        copy.topP = topP
        copy.topK = topK
        copy.maxOutputTokens = maxOutputTokens
        copy.region = region

        return copy
    }

    override fun panel() = AmazonBedrockClientPanel(this)

    class RegionConverter : Converter<Region>() {
        override fun toString(value: Region): String? {
            return value.toString()
        }

        override fun fromString(value: String): Region? {
            return Region.regions().find { it.id() == value }
        }
    }
}
