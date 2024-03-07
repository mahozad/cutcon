package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultCalendar
import ir.mahozad.cutcon.model.Calendar
import ir.mahozad.cutcon.model.PreferenceKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.util.prefs.Preferences

@OptIn(ExperimentalCoroutinesApi::class)
abstract class CalendarTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the calendar should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.calendar.first()
        assertThat(result).isEqualTo(defaultCalendar)
    }

    @Test
    fun `When settings has calendar, the calendar should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_CALENDAR, Calendar.GREGORIAN.name)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<Calendar>()
        backgroundScope.launch(dispatcher) { viewModel.calendar.toList(results) }
        assertThat(results).containsExactly(Calendar.GREGORIAN)
    }

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Fails on CI; FIXME")
    @Test
    fun `After changing calendar, the calendar should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Calendar>()
        backgroundScope.launch(dispatcher) { viewModel.calendar.toList(results) }
        viewModel.setCalendar(Calendar.GREGORIAN)
        assertThat(results).containsExactly(defaultCalendar, Calendar.GREGORIAN)
    }

    @Test
    fun `After changing calendar, the calendar should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setCalendar(Calendar.GREGORIAN)
        val calendar = settings[PreferenceKeys.PREF_CALENDAR, null]?.let(Calendar::valueOf)
        assertThat(calendar).isEqualTo(Calendar.GREGORIAN)
    }
}
