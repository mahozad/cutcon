package ir.mahozad.cutcon.component

import java.time.LocalDate
import java.time.LocalTime

object SystemDateTime {
    fun nowTime(): LocalTime = LocalTime.now()
    fun nowDate(): LocalDate = LocalDate.now()
    fun nowMillis(): Long = System.currentTimeMillis()
}
