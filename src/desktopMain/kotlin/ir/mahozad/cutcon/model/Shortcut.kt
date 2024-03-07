package ir.mahozad.cutcon.model

import androidx.compose.ui.input.key.Key

enum class Shortcut(
    val keys: List<Key>,
    val symbol: String
) {
    SEEK_SHORT_BACKWARD( symbol = "\uD83E\uDC04", keys = listOf(Key.DirectionLeft)),
    SEEK_SHORT_FORWARD(  symbol = "\uD83E\uDC06", keys = listOf(Key.DirectionRight)),
    SEEK_LONG_BACKWARD(  symbol = "Page Up",      keys = listOf(Key.PageUp)),
    SEEK_LONG_FORWARD(   symbol = "Page Down",    keys = listOf(Key.PageDown)),
    FULLSCREEN_TOGGLE(   symbol = "F",            keys = listOf(Key.F)),
    FULLSCREEN_EXIT(     symbol = "Esc",          keys = listOf(Key.Escape)),
    MINI_MODE_TOGGLE(    symbol = ".",            keys = listOf(Key.Period)),
    SIDE_PANEL_TOGGLE(   symbol = "/",            keys = listOf(Key.Slash)),
    CLIP_LOOP_TOGGLE(    symbol = "R",            keys = listOf(Key.R)),
    CLIP_START_BEGINNING(symbol = "0",            keys = listOf(Key.Zero,     Key.NumPad0)),
    CLIP_START_NOW(      symbol = "1",            keys = listOf(Key.One,      Key.NumPad1)),
    CLIP_END_NOW(        symbol = "2",            keys = listOf(Key.Two,      Key.NumPad2)),
    CLIP_END_FINISH(     symbol = "3",            keys = listOf(Key.Three,    Key.NumPad3)),
    SPEED_RESET(         symbol = "*",            keys = listOf(Key.Multiply, Key.NumPadMultiply)),
    SPEED_DECREASE(      symbol = "-",            keys = listOf(Key.Minus,    Key.NumPadSubtract)),
    SPEED_INCREASE(      symbol = "+",            keys = listOf(Key.Plus,     Key.NumPadAdd)),
    AUDIO_MUTE_TOGGLE(   symbol = "M",            keys = listOf(Key.M)),
    AUDIO_DECREASE(      symbol = "\uD83E\uDC05", keys = listOf(Key.DirectionDown)),
    AUDIO_INCREASE(      symbol = "\uD83E\uDC07", keys = listOf(Key.DirectionUp)),
    LIVE_PLAY(           symbol = "L",            keys = listOf(Key.L)),
    PLAY_PAUSE(          symbol = "Space",        keys = listOf(Key.Spacebar)),
    PIN_TOGGLE(          symbol = "P",            keys = listOf(Key.P)),
    SCREENSHOT_TAKE(     symbol = "S",            keys = listOf(Key.S))
}
