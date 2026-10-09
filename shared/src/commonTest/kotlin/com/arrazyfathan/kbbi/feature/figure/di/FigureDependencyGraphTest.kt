package com.arrazyfathan.kbbi.feature.figure.di

import com.arrazyfathan.kbbi.di.useCaseModule
import com.arrazyfathan.kbbi.feature.figure.data.di.figureDataModule
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFigureDetailUseCase
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFiguresUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test

class FigureDependencyGraphTest {
    @Test
    fun koinResolvesFigureRepositoryAndUseCasesAtRuntime() {
        val app =
            koinApplication {
                modules(
                    module {
                        single {
                            HttpClient(
                                MockEngine {
                                    respond(
                                        "{}",
                                        headers =
                                            headersOf(
                                                HttpHeaders.ContentType,
                                                "application/json",
                                            ),
                                    )
                                },
                            )
                        }
                    },
                    figureDataModule,
                    useCaseModule,
                )
            }

        app.koin.get<FigureRepository>()
        app.koin.get<GetFiguresUseCase>()
        app.koin.get<GetFigureDetailUseCase>()
        app.close()
    }
}
