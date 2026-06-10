package com.arrazyfathan.kbbi.di

import com.arrazyfathan.kbbi.core.di.networkModule
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksViewModel
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailViewModel
import com.arrazyfathan.kbbi.feature.home.data.di.databaseModule
import com.arrazyfathan.kbbi.feature.home.data.di.repositoryModule
import com.arrazyfathan.kbbi.feature.home.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordViewModel
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
        factoryOf(::SearchWordUseCase)
        factoryOf(::SearchWordWithHistoryUseCase)
        factoryOf(::AddSearchHistoryUseCase)
        factoryOf(::ObserveSearchHistoryUseCase)
        factoryOf(::SaveBookmarkUseCase)
        factoryOf(::DeleteBookmarkUseCase)
        factoryOf(::CheckWordSavedUseCase)
        factoryOf(::ObserveBookmarksUseCase)
        factoryOf(::GetWordEntriesUseCase)
    }

val viewModelModule =
    module {
        viewModelOf(::DetailViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::BookmarksViewModel)
        viewModelOf(::WordViewModel)
    }

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        includes(config)
        modules(
            platformModule,
            databaseModule,
            repositoryModule,
            viewModelModule,
            networkModule,
            useCaseModule,
        )
    }
}
