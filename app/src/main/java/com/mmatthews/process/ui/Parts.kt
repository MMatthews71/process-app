package com.mmatthews.process.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/** Single column, centred vertically, scrolls only if it has to. */
@Composable
fun Centered(
    modifier: Modifier = Modifier.fillMaxSize(),
    spacing: Int = 32,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
fun AccentButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val p = LocalPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = p.accent,
            contentColor = p.background,
            disabledContainerColor = p.accent.copy(alpha = 0.35f),
            disabledContentColor = p.background,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Text(text, style = TextSizes.Normal)
    }
}

/** A text field with no border or background: just the text, or the placeholder when empty. */
@Composable
fun PlainField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    val p = LocalPalette.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextSizes.Normal.copy(color = p.text),
        cursorBrush = SolidColor(p.accent),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = TextSizes.Normal, color = p.text.copy(alpha = 0.5f))
                }
                inner()
            }
        },
    )
}

/** Wrapping row of selectable text pills. The chosen one is filled with the accent colour. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Pills(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    val p = LocalPalette.current
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                option,
                style = TextSizes.Normal,
                color = if (isSelected) p.background else p.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) p.accent else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.RadioButton,
                        onClick = { onSelect(option) },
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
