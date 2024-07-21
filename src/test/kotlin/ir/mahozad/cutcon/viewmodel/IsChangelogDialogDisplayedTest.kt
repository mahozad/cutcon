package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
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
import java.util.prefs.Preferences

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IsChangelogDialogDisplayedTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    // @Test
    // fun `When settings has no app changelog version, isChangelogDialogDisplayed should be true`() = runTest {
    //     val dispatcher = UnconfinedTestDispatcher(testScheduler)
    //     val viewModel = constructMainViewModel(
    //         dispatcher = dispatcher,
    //         settings = settings
    //     )
    //     assertThat(viewModel.isChangelogDialogDisplayed.first()).isEqualTo(true)
    // }

    // @Test
    // fun `When settings has changelog version that is less than current app version, isChangelogDialogDisplayed should be true`() =
    //     runTest {
    //         settings.put(PreferenceKeys.PREF_LAST_SHOWN_CHANGELOG_VERSION, "0.0.0")
    //         val dispatcher = UnconfinedTestDispatcher(testScheduler)
    //         val viewModel = constructMainViewModel(
    //             dispatcher = dispatcher,
    //             settings = settings
    //         )
    //         assertThat(viewModel.isChangelogDialogDisplayed.first()).isEqualTo(true)
    //     }

    @Test
    fun `When settings has changelog version that is equal to current app version, isChangelogDialogDisplayed should be false`() =
        runTest {
            settings.put(PreferenceKeys.PREF_LAST_SHOWN_CHANGELOG_VERSION, BuildConfig.APP_VERSION)
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            assertThat(viewModel.isChangelogDialogDisplayed.first()).isEqualTo(false)
        }

    @Test
    fun `When settings has changelog version that is greater than current app version, isChangelogDialogDisplayed should be false`() =
        runTest {
            settings.put(
                PreferenceKeys.PREF_LAST_SHOWN_CHANGELOG_VERSION,
                BuildConfig.APP_VERSION.replaceBefore('.', "9${BuildConfig.APP_VERSION.substringBefore('.')}")
            )
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            assertThat(viewModel.isChangelogDialogDisplayed.first()).isEqualTo(false)
        }

    // @Test
    // fun `After showing changelog dialog, isChangelogDialogDisplayed should change to true`() = runTest {
    //     val dispatcher = UnconfinedTestDispatcher(testScheduler)
    //     val viewModel = constructMainViewModel(dispatcher)
    //     val results = mutableListOf<Boolean>()
    //     backgroundScope.launch(dispatcher) { viewModel.isChangelogDialogDisplayed.toList(results) }
    //     viewModel.showChangelogDialog()
    //     assertThat(results).containsExactly(true)
    // }

    @Test
    fun `After dismissing changelog dialog, isChangelogDialogDisplayed should change to false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            showChangelogDialog()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isChangelogDialogDisplayed.toList(results) }
        viewModel.onChangelogDialogDismissRequest()
        assertThat(results).containsExactly(true, false)
    }
}
