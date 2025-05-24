package ir.mahozad.cutcon.component

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.defaultDateTimeCheckingPeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime
import kotlin.coroutines.CoroutineContext

fun interface DateTimeChecker {
    fun dateTimeFlow(): Flow<Pair<LocalDate, LocalTime>>
}

class DefaultDateTimeChecker(private val dispatcher: CoroutineContext) : DateTimeChecker {

    private val logger = logger(name = javaClass.simpleName)

    override fun dateTimeFlow() = flow {
        while (true) {
            val date = SystemDateTime.nowDate()
            val time = SystemDateTime
                .nowTime()
                .withSecond(0)
                .withNano(0)
            emit(date to time)
            delay(defaultDateTimeCheckingPeriod)
        }
    }
        .distinctUntilChanged()
        .onEach { logger.debug { "Detected date or time change: $it" } }
        .flowOn(dispatcher)
}
