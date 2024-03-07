package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultLanguage
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageEn
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
abstract class LanguageTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the language should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.language.first()
        assertThat(result).isEqualTo(defaultLanguage)
    }

    @Test
    fun `When settings has language, the language should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_LANGUAGE, LanguageEn.tag)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<Language>()
        backgroundScope.launch(dispatcher) { viewModel.language.toList(results) }
        assertThat(results).containsExactly(LanguageEn)
    }

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Fails on CI; FIXME")
    @Test
    fun `After changing language, the language should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Language>()
        backgroundScope.launch(dispatcher) { viewModel.language.toList(results) }
        viewModel.setLanguage(LanguageEn)
        assertThat(results).containsExactly(defaultLanguage, LanguageEn)
    }

    @Test
    fun `After changing language, the language should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setLanguage(LanguageEn)
        val language = settings[PreferenceKeys.PREF_LANGUAGE, null]?.let(Language::fromTag)
        assertThat(language).isEqualTo(LanguageEn)
    }
}
