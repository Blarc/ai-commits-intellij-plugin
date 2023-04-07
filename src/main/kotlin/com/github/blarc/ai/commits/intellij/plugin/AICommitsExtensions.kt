package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.ui.ColumnInfo

fun <T> createColumn(name: String, formatter: (T) -> String) : ColumnInfo<T, String> {
    return object : ColumnInfo<T, String>(name) {
        override fun valueOf(item: T): String {
            return formatter(item)
        }
    }
}

fun ValidationInfoBuilder.notBlank(value: String): ValidationInfo? =
        if (value.isBlank()) error(message("validation.required")) else null

fun ValidationInfoBuilder.unique(value: String, existingValues: Set<String>): ValidationInfo? =
        if (existingValues.contains(value)) error(message("validation.unique")) else null

fun ValidationInfoBuilder.isLong(value: String): ValidationInfo? {
    if (value.isBlank()){
        return null
    }

    value.toLongOrNull().let {
        if (it == null) {
            return error(message("validation.number"))
        } else {
            return null
        }
    }
}
