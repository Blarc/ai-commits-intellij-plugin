package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class PromptTextArea(initialText: String = "") : JPanel(BorderLayout()) {

    companion object {
        val AVAILABLE_VARIABLES = listOf(
            "{branch}",
            "{diff}",
            "{locale}",
            "{hint}",
            "{taskId}",
            "{taskSummary}",
            "{taskDescription}",
            "{taskTimeSpent}"
        )
    }

    val textArea = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        rows = 5
        autoscrolls = false
        text = initialText
    }

    init {
        // Create the main text area in a scroll pane
        val scrollPane = JBScrollPane(textArea).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = null
        }

        // Create the variables panel at the bottom
        val variablesPanel = createVariablesPanel()

        add(scrollPane, BorderLayout.CENTER)
        add(variablesPanel, BorderLayout.SOUTH)
    }

    fun addOnChangeListener(onTextChanged: ((String) -> Unit)) {
        textArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = onTextChanged(textArea.text)
            override fun removeUpdate(e: DocumentEvent?) = onTextChanged(textArea.text)
            override fun changedUpdate(e: DocumentEvent?) = onTextChanged(textArea.text)
        })
    }

    private fun createVariablesPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
            background = textArea.background
        }
        panel.add(JLabel("Add a variable:")).apply {
            // Makes it look like a comment
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        }
        AVAILABLE_VARIABLES.forEach { variable ->
            panel.add(createVariableLabel(variable))
        }

        return panel
    }

    private fun createVariableLabel(variable: String): JLabel {
        return object : JLabel(variable) {
            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Draw a rounded background
                g2.color = background
                g2.fillRoundRect(0, 0, width, height, 8, 8) // 8px corner radius

                // Draw the text
                super.paintComponent(g2)
                g2.dispose()
            }
        }.apply {
            foreground = JBUI.CurrentTheme.Label.foreground()
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            border = JBUI.Borders.empty(4, 8)
            background = JBUI.CurrentTheme.ActionButton.hoverBackground()
            isOpaque = false // Set to false since we're custom painting the background

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    insertVariableAtCursor(variable)
                }

                override fun mouseEntered(e: MouseEvent) {
                    background = JBUI.CurrentTheme.ActionButton.pressedBackground()
                    repaint()
                }

                override fun mouseExited(e: MouseEvent) {
                    background = JBUI.CurrentTheme.ActionButton.hoverBackground()
                    repaint()
                }
            })
        }
    }

    private fun insertVariableAtCursor(variable: String) {
        val caretPosition = textArea.caretPosition
        val currentText = textArea.text

        val newText = currentText.take(caretPosition) +
                variable +
                currentText.substring(caretPosition)

        textArea.text = newText
        textArea.caretPosition = caretPosition + variable.length
        textArea.requestFocus()
    }

    // Delegate methods to make it easy to use like a regular text area
    var text: String
        get() = textArea.text
        set(value) {
            textArea.text = value
        }

    var isEditable: Boolean
        get() = textArea.isEditable
        set(value) {
            textArea.isEditable = value
        }
}
