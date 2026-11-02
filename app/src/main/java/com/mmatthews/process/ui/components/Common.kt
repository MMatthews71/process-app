package com.mmatthews.process.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mmatthews.process.domain.TextRules
import kotlin.math.roundToInt

/** Standard screen: back + skip on top, scrolling content, actions at the bottom. */
@Composable
fun FlowScaffold(
    onBack: (() -> Unit)?,
    onSkip: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Spacer(Modifier.weight(1f))
            if (onSkip != null) {
                TextButton(onClick = onSkip, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Skip", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = bottom,
        )
    }
}

@Composable
fun Prompt(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = 60.dp)) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().heightIn(min = 60.dp)) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** A large tappable option; filled when selected. */
@Composable
fun ChoiceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
) {
    val m = (if (fullWidth) modifier.fillMaxWidth() else modifier).heightIn(min = 56.dp)
    if (selected) {
        Button(onClick = onClick, modifier = m) { Text(text, style = MaterialTheme.typography.titleMedium) }
    } else {
        OutlinedButton(onClick = onClick, modifier = m) { Text(text, style = MaterialTheme.typography.titleMedium) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceRow(content: @Composable () -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

/** 0–10 slider with the value shown large. */
@Composable
fun IntensitySlider(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth().testTag("intensitySlider"),
        )
        Row(Modifier.fillMaxWidth()) {
            Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("10", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Text field with a mic button that appends dictated text. */
@Composable
fun VoiceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    minLines: Int = 2,
    singleLine: Boolean = false,
) {
    val voice = LocalVoiceInput.current
    val listening = voice.activeKey == testTag
    val current by rememberUpdatedState(value)
    val onChange by rememberUpdatedState(onValueChange)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (listening) voice.stop()
                        else voice.start(testTag) { spoken -> onChange(TextRules.appendSpoken(current, spoken)) }
                    },
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        AppIcons.Mic,
                        contentDescription = if (listening) "Stop dictation" else "Dictate",
                        tint = if (listening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }
}
