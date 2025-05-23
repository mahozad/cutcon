package ir.mahozad.cutcon.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.icon.CheckMark
import ir.mahozad.cutcon.ui.icon.Icons
import ir.mahozad.cutcon.ui.icon.Warn
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.cutcon.ui.theme.success
import ir.mahozad.cutcon.ui.widget.StackTrace
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Preview
@Composable
private fun SuccessDialogPreviewFa() {
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            SuccessDialog(
                totalTime = 173.minutes,
                onCloseRequest = {}
            )
        }
    }
}

@Preview
@Composable
private fun SuccessDialogPreviewEn() {
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            SuccessDialog(
                totalTime = 173.minutes,
                onCloseRequest = {}
            )
        }
    }
}

@Preview
@Composable
private fun FailureDialogPreviewFa() {
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            FailureDialog(
                throwable = IllegalArgumentException(),
                onCloseRequest = {}
            )
        }
    }
}

@Preview
@Composable
private fun FailureDialogPreviewEn() {
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            FailureDialog(
                throwable = IllegalArgumentException(),
                onCloseRequest = {}
            )
        }
    }
}

@Composable
fun SuccessDialog(totalTime: Duration?, onCloseRequest: () -> Unit) {
    val language = LocalLanguage.current
    FinishDialog(
        modifier = Modifier.size(width = 230.dp, height = 96.dp),
        onCloseRequest = onCloseRequest
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Custom.CheckMark,
                    contentDescription = Messages.ICO_DSC_SUCCESS,
                    tint = MaterialTheme.colors.success
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = language.messages.txtLblClipCreationSuccess, fontSize = 14.sp)
            }
            Row {
                Spacer(Modifier.width(28.dp))
                totalTime?.let {
                    Text(
                        text = "(${language.messages.totalClipCreationTime(totalTime)})",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FailureDialog(throwable: Throwable?, onCloseRequest: () -> Unit) {
    FinishDialog(
        modifier = Modifier.size(width = 420.dp, height = 380.dp),
        onCloseRequest = onCloseRequest
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Custom.Warn,
                    contentDescription = Messages.ICO_DSC_FAILURE,
                    tint = MaterialTheme.colors.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = LocalLanguage.current.messages.txtLblClipCreationFailure,
                    fontSize = 14.sp
                )
            }
            Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
                StackTrace(throwable)
            }
        }
    }
}

/**
 * See https://github.com/JetBrains/compose-multiplatform/issues/3438
 */
@Composable
private fun FinishDialog(
    modifier: Modifier,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val language = LocalLanguage.current
    // See https://github.com/JetBrains/compose-multiplatform/issues/3438
    AnimatedDialog(
        title = language.messages.appName,
        modifier = modifier,
        onDismissRequest = onCloseRequest
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            content()
            Spacer(Modifier.height(10.dp))
        }
    }
}
