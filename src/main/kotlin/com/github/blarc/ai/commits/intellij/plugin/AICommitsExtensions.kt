package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ComponentWithEmptyText
import javax.swing.JComponent

fun <T, O> createColumn(name: String, formatter: (T) -> O) : ColumnInfo<T, O> {
    return object : ColumnInfo<T, O>(name) {
        override fun valueOf(item: T): O {
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

fun String.wrap(length: Int): String {
    var input = this
    val wrapped = StringBuilder()

    while (input.length > length) {
        var index = input.lastIndexOf(' ', length)

        if (index == -1) index = length
        wrapped.append(input.substring(0, index)).append("<br>")

        input = input.substring(index).trimStart()
    }

    wrapped.append(input)

    return wrapped.toString()
}
