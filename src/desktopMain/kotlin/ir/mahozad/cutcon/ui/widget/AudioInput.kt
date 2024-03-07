package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.MainViewModel
import ir.mahozad.cutcon.defaultIconSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.Shortcut
import ir.mahozad.cutcon.ui.icon.*

@Composable
fun AudioInput(
    viewModel: MainViewModel,
    iconPadding: Dp,
    mainModifier: Modifier,
    sliderModifier: Modifier,
) {
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = mainModifier
    ) {
        Tooltip(
            if (mediaInfo.isAudioMuted) {
                LocalLanguage.current.messages.unMuteMediaAudio
            } else {
                LocalLanguage.current.messages.muteMediaAudio
            },
            Shortcut.AUDIO_MUTE_TOGGLE.symbol,
            Shortcut.AUDIO_DECREASE.symbol,
            Shortcut.AUDIO_INCREASE.symbol
        ) {
            IconButton(onClick = viewModel::toggleAudioMute) {
                Icon(
                    imageVector = if (mediaInfo.isAudioMuted) {
                        Icons.Custom.VolumeMuted
                    } else if (mediaInfo.audioVolume > 0.66) {
                        Icons.Custom.VolumeFull
                    } else if (mediaInfo.audioVolume > 0.05) {
                        Icons.Custom.VolumeHalf
                    } else {
                        Icons.Custom.VolumeEmpty
                    },
                    contentDescription = Messages.ICO_DSC_AUDIO_VOLUME,
                    modifier = Modifier.padding(iconPadding).size(defaultIconSize)
                )
            }
        }
        // TODO: Make the slider change volume in logarithmic manner
        //  See https://www.dr-lex.be/info-stuff/volumecontrols.html
        //  and https://ux.stackexchange.com/q/79672/117386
        //  and https://dcordero.me/posts/logarithmic_volume_control.html
        Slider(
            value = mediaInfo.audioVolume,
            onValueChange = viewModel::setVolume,
            modifier = sliderModifier
        )
    }
}
