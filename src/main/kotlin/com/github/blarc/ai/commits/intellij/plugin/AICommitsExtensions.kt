package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ComponentWithEmptyText
import javax.swing.JComponent

fun <T> createColumn(name: String, formatter: (T) -> String) : ColumnInfo<T, String> {
    return object : ColumnInfo<T, String>(name) {
        override fun valueOf(item: T): String {
            return formatter(item)
        }
    }
}

fun ValidationInfoBuilder.notBlank(value: String): ValidationInfo? =
        if (value.isBlank()) error(message("validation.required")) else null


fun ValidationInfoBuilder.temperatureValid(value: String): ValidationInfo?  {
    if (value.isNotBlank()) {
        value.toFloatOrNull().let {
            if (it != null && it in 0.0..2.0) {
                return null
            }
        }
    }
    return error(message("validation.temperature"))
}

fun ValidationInfoBuilder.unique(value: String, existingValues: Set<String>): ValidationInfo? =
        if (existingValues.contains(value)) error(message("validation.unique")) else null

fun ValidationInfoBuilder.isInt(value: String): ValidationInfo? {
    if (value.isBlank()){
        return null
    }

    value.toIntOrNull().let {
        if (it == null) {
            return error(message("validation.integer"))
        } else {
            return null
        }
    }
}

// Adds emptyText method to all cells that contain a component that implements ComponentWithEmptyText class
fun <T>Cell<T>.emptyText(emptyText: String) : Cell<T> where T : JComponent, T : ComponentWithEmptyText {
    this.component.emptyText.text = emptyText
    return this
}
