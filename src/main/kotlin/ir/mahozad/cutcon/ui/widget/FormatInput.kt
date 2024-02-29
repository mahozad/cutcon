package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.mahozad.cutcon.model.Format

@Preview
@Composable
private fun FormatInputPreview() {
    FormatInput(isEnabled = true, format = Format.MP4, onChange = {})
}

@Composable
fun FormatInput(
    isEnabled: Boolean,
    format: Format,
    onChange: (Format) -> Unit
) {
    RadioGroup(
        value = format,
        isEnabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
        weights = { if (it == 0) 1.1f else if (it == 1) 0.9f else 1.2f },
        onChange = onChange
    )
}
