package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.*

@Preview
@Composable
private fun TimestampInputPreview() {
    TimestampInput(
        minuteInput = TextFieldValue(text = "12", selection = TextRange.Two),
        secondInput = TextFieldValue(text = "37", selection = TextRange.Two),
        startRoundness = 24.dp,
        onMinuteChange = { _, _ -> },
        onSecondChange = { _, _ -> },
    )
}

@Preview
@Composable
private fun CustomTimestampTextFieldPreview() {
    CustomTimestampTextField(
        value = TextFieldValue(text = "11", selection = TextRange.Zero),
        placeholder = "00",
        startRoundness = 24.dp,
        endRoundness = 0.dp,
        onValueChange = { _, _ -> },
    )
}

@Composable
fun TimestampInput(
    minuteInput: TextFieldValue,
    secondInput: TextFieldValue,
    startRoundness: Dp = 0.dp,
    endRoundness: Dp = 0.dp,
    onMinuteChange: (String, TextRange) -> Unit,
    onSecondChange: (String, TextRange) -> Unit,
) {
    Row {
        CustomTimestampTextField(
            value = minuteInput,
            placeholder = LocalLanguage.current.localizeDigits(defaultTimeStampString),
            startRoundness = startRoundness,
            endRoundness = 0.dp,
            onValueChange = onMinuteChange
        )
        Spacer(Modifier.width(1.dp))
        CustomTimestampTextField(
            value = secondInput,
            placeholder = LocalLanguage.current.localizeDigits(defaultTimeStampString),
            startRoundness = 0.dp,
            endRoundness = endRoundness,
            onValueChange = onSecondChange
        )
    }
}

/**
 * See https://stackoverflow.com/a/73147836
 * and https://stackoverflow.com/a/69267008
 *
 * We used TextFieldValue instead of the simple string as the field text
 * to be able to select all the text on mouse click (focus).
 * Probably could use [SelectionContainer] instead.
 * Could simply use ` BasicTextField(value = value,...` and let go of
 * the TextFieldValue and all its ceremony and custom code.
 * See the commit bf44d702edaa9de07295a24cab93f87f574dd05a.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CustomTimestampTextField(
    value: TextFieldValue,
    placeholder: String,
    startRoundness: Dp,
    endRoundness: Dp,
    onValueChange: (String, TextRange) -> Unit
) {
    var textFieldValue by remember(value) { mutableStateOf(value) }
    val interactionSource = remember(::MutableInteractionSource)
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        textFieldValue = textFieldValue.copy(
            selection = if (isFocused) {
                TextRange(start = 0, end = textFieldValue.text.length)
            } else {
                TextRange.Zero
            }
        )
    }
    BasicTextField(
        value = textFieldValue,
        onValueChange = { onValueChange(it.text, it.selection) },
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        textStyle = LocalTextStyle.current.copy(
            fontSize = defaultFontSize,
            textAlign = TextAlign.Center,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.high)
        ),
        interactionSource = interactionSource,
        enabled = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            // To modify the width, also, modify the content padding start and end values in the TextFieldDecoration below
            .width(if (startRoundness == endRoundness && startRoundness == 0.dp) 40.dp else 44.dp)
            .height(defaultInputHeight)
            .clip(
                RoundedCornerShape(
                    topStart = startRoundness,
                    bottomStart = startRoundness,
                    topEnd = endRoundness,
                    bottomEnd = endRoundness
                )
            )
            .onKeyEvent { event ->
                handleKeyEvent(event, textFieldValue)
                    ?.let { textFieldValue = it }
                // Should consume (true) (except for TAB) to prevent bugs with app custom shortcuts
                // See https://github.com/JetBrains/compose-multiplatform/issues/1925
                event.key != Key.Tab
            }
    ) {
        TextFieldDefaults.TextFieldDecorationBox(
            value = value.text,
            innerTextField = it,
            singleLine = true,
            enabled = true,
            placeholder = {
                Text(
                    text = placeholder,
                    style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    fontSize = (defaultFontSize.value - 3).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            // Keeps horizontal paddings but changes the vertical
            contentPadding = TextFieldDefaults.textFieldWithoutLabelPadding(
                top = 0.dp,
                bottom = 0.dp,
                // To modify these, also, modify the parent BasicTextField Modifier.width above
                start = if (endRoundness == startRoundness) 10.dp else if (endRoundness > 0.dp) 9.dp else 15.dp,
                end = if (endRoundness == startRoundness) 10.dp else if (startRoundness > 0.dp) 9.dp else 15.dp
            )
        )
    }
}

private fun handleKeyEvent(event: KeyEvent, textFieldValue: TextFieldValue): TextFieldValue? {
    return if (event.type != KeyEventType.KeyDown) {
        null
    } else if (event.isShiftPressed && event.key == Key.DirectionLeft) {
        textFieldValue.copy(
            selection = TextRange(
                (textFieldValue.selection.start - 1).coerceAtLeast(0),
                textFieldValue.selection.end
            )
        )
    } else if (event.key == Key.DirectionLeft && textFieldValue.selection.length > 0) {
        textFieldValue.copy(selection = TextRange(textFieldValue.selection.start))
    } else if (event.key == Key.DirectionLeft) {
        textFieldValue.copy(
            selection = TextRange((textFieldValue.selection.end - 1).coerceAtLeast(0))
        )
    } else if (event.isShiftPressed && event.key == Key.DirectionRight) {
        textFieldValue.copy(
            selection = TextRange(
                textFieldValue.selection.start,
                (textFieldValue.selection.end + 1).coerceAtMost(2)
            )
        )
    } else if (event.key == Key.DirectionRight && textFieldValue.selection.length > 0) {
        textFieldValue.copy(selection = TextRange(textFieldValue.selection.end))
    } else if (event.key == Key.DirectionRight) {
        textFieldValue.copy(
            selection = TextRange((textFieldValue.selection.start + 1).coerceAtMost(2))
        )
    } else {
        null
    }
}
