package ir.mahozad.cutcon.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

/**
 * See https://stackoverflow.com/a/73338484
 * and the logback.xml file in the classpath.
 */
class CustomLevelHighlighter : ForegroundCompositeConverterBase<ILoggingEvent>() {
    override fun getForegroundColorCode(event: ILoggingEvent) =
        when (event.level) {
            Level.ERROR -> ANSIConstants.RED_FG /* + ANSIConstants.BOLD */
            Level.WARN  -> ANSIConstants.YELLOW_FG
            Level.INFO  -> ANSIConstants.BLUE_FG
            Level.DEBUG -> ANSIConstants.CYAN_FG
            Level.TRACE -> ANSIConstants.GREEN_FG
            else        -> ANSIConstants.DEFAULT_FG
        }
}
