package com.arrazyfathan.kbbi.di

import com.arrazyfathan.kbbi.core.di.networkModule
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.observability.ReportingBackend
import com.arrazyfathan.kbbi.core.observability.ReportingController
import com.arrazyfathan.kbbi.core.observability.observabilityModule
import com.arrazyfathan.kbbi.core.platform.Haptics
import com.arrazyfathan.kbbi.core.platform.SettingsAwareHaptics
import com.arrazyfathan.kbbi.feature.bookmark.data.BookmarkLayoutPreferenceStore
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksViewModel
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailViewModel
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.WordDetailRouteViewModel
import com.arrazyfathan.kbbi.feature.figure.data.di.figureDataModule
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFigureDetailUseCase
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFiguresUseCase
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureDetailViewModel
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureViewModel
import com.arrazyfathan.kbbi.feature.home.data.di.databaseModule
import com.arrazyfathan.kbbi.feature.home.data.di.repositoryModule
import com.arrazyfathan.kbbi.feature.home.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetTopWordsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.RestoreWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.feature.proverb.data.di.proverbRepositoryModule
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.GetListProverbsUseCase
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.GetProverbMeaningUseCase
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbViewModel
import com.arrazyfathan.kbbi.feature.reminders.data.ReminderContentProvider
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.presentation.SettingsViewModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordViewModel
import com.arrazyfathan.kbbi.feature.wordstudy.data.di.wordStudyDataModule
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GenerateWordStudyUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GetAiProvidersUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.RemoveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SaveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectAiProviderModeUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectBackendAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectCustomAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.TestAiConnectionUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai.AiSettingsViewModel
import com.arrazyfathan.kbbi.platformModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

val useCaseModule =
    module {
        single { BookmarkLayoutPreferenceStore() }
        single { SettingsPreferenceStore(get(), get()) }
        single { ReminderContentProvider(get(), get(), get()) }
        single<Haptics> { SettingsAwareHaptics(get()) { get<SettingsPreferenceStore>().preferences.value.hapticsEnabled } }
        factoryOf(::SearchWordUseCase)
        factoryOf(::RestoreWordUseCase)
        factoryOf(::SearchWordWithHistoryUseCase)
        factoryOf(::AddSearchHistoryUseCase)
        factoryOf(::ObserveSearchHistoryUseCase)
        factoryOf(::SaveBookmarkUseCase)
        factoryOf(::DeleteBookmarkUseCase)
        factoryOf(::CheckWordSavedUseCase)
        factoryOf(::ObserveBookmarksUseCase)
        factoryOf(::GetWordEntriesUseCase)
        factoryOf(::GetTopWordsUseCase)
        factoryOf(::GetWordSuggestionsUseCase)
        factoryOf(::GetWordTranslationUseCase)
        factoryOf(::GetListProverbsUseCase)
        factoryOf(::GetProverbMeaningUseCase)
        factoryOf(::GetFiguresUseCase)
        factoryOf(::GetFigureDetailUseCase)
        factoryOf(::ClearSearchHistoryUseCase)
        factoryOf(::GenerateWordStudyUseCase)
        factoryOf(::GetAiProvidersUseCase)
        factoryOf(::SaveAiConfigurationUseCase)
        factoryOf(::SelectAiProviderModeUseCase)
        factoryOf(::SelectBackendAiProviderUseCase)
        factoryOf(::SelectCustomAiProviderUseCase)
        factoryOf(::RemoveAiConfigurationUseCase)
        factoryOf(::TestAiConnectionUseCase)
    }

val viewModelModule =
    module {
        viewModelOf(::DetailViewModel)
        viewModelOf(::AiSettingsViewModel)
        viewModelOf(::WordDetailRouteViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::BookmarksViewModel)
        viewModelOf(::WordViewModel)
        viewModelOf(::ProverbViewModel)
        viewModelOf(::FigureViewModel)
        viewModelOf(::FigureDetailViewModel)
        viewModelOf(::SettingsViewModel)
    }

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication =
    startKoin {
        includes(config)
        modules(
            platformModule,
            databaseModule,
            repositoryModule,
            proverbRepositoryModule,
            figureDataModule,
            wordStudyDataModule,
            viewModelModule,
            networkModule,
            useCaseModule,
            observabilityModule,
        )
    }.also { application ->
        val controller = application.koin.get<ReportingController>()
        controller.synchronizeCollection()
        AppLogger.installRemoteSink(controller)
        AppLogger.configureDiagnostics(
            application.koin
                .get<ReportingBackend>()
                .buildInfo.buildType == "debug",
        )
    }
