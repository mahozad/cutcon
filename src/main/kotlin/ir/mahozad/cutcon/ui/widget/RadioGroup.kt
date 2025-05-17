package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.model.Labeled

@Suppress("unused")
private enum class TestEnum(override val label: (Language) -> String) : Labeled {
    TEST1({ "Test 1" }),
    TEST2({ "Test 2" }),
    TEST3({ "Test 3" })
}

@Preview
@Composable
private fun RadioGroupPreview() {
    RadioGroup(value = TestEnum.TEST1, isEnabled = true, weights = { 1f })
}

@Composable
fun <T> RadioGroup(
    value: T,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    weights: (index: Int) -> Float?,
    onChange: (T) -> Unit = {}
) where T : Enum<T>, T : Labeled {
    Row(
        modifier.selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for ((i, option) in value.declaringJavaClass.enumConstants.withIndex() /* OR enumValues<T>() */) {
            RadioOption(
                text = option.label(LocalLanguage.current),
                isEnabled = isEnabled,
                modifier = Modifier.then(weights(i)?.let { Modifier.weight(it) } ?: Modifier),
                isSelected = (value == option)
            ) {
                onChange(option)
            }
        }
    }
}

/**
 * Implemented as described [here](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary).
 *
 * For my previous approach see [this commit](3e4ae87bb615d8b8321ab9a3bb7c0f0c9549bbe8).
 */
@Composable
private fun RadioOption(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        RadioButton(
            selected = isSelected,
            enabled = isEnabled,
            onClick = null // For accessibility with screen readers
        )
        Text(
            text = text,
            color = LocalContentColor.current.copy(alpha = if (isEnabled) LocalContentAlpha.current else ContentAlpha.disabled),
            fontSize = (defaultFontSize.value - 1).sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
