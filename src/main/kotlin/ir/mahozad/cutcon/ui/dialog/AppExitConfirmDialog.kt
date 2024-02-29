package ir.mahozad.cutcon.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.ui.DialogDecoration
import ir.mahozad.cutcon.ui.theme.AppTheme

@Preview
@Composable
private fun AppExitConfirmDialogPreviewFa() {
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            AppExitConfirmDialog({}, {})
        }
    }
}

@Preview
@Composable
private fun AppExitConfirmDialogPreviewEn() {
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            AppExitConfirmDialog({}, {})
        }
    }
}

@Composable
fun AppExitConfirmDialog(onDenied: () -> Unit, onConfirmed: () -> Unit) {
    val language = LocalLanguage.current
    Dialog(/* Called when clicking outside the dialog: */ onDismissRequest = onDenied) {
        DialogDecoration(
            icon = painterResource("logo.svg"),
            title = { Text(text = language.messages.appName, fontSize = (defaultFontSize.value - 1).sp) },
            modifier = Modifier.size(width = 360.dp, height = 146.dp),
            onCloseRequest = onDenied
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.height(36.dp)) {
                    Text(text = language.messages.clipCreationIsAbandonedIfExitTheApp, fontSize = defaultFontSize)
                    Text(text = language.messages.areYouSureToExitTheApp, fontSize = defaultFontSize)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDenied, modifier = Modifier.width(64.dp)) {
                        Text(text = language.messages.no, fontSize = defaultFontSize)
                    }
                    Spacer(Modifier.width(48.dp))
                    Button(onClick = onConfirmed, modifier = Modifier.width(64.dp)) {
                        Text(text = language.messages.yes, fontSize = defaultFontSize)
                    }
                }
            }
        }
    }
}
