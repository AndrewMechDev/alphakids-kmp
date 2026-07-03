package org.alphakids.app.game.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.game.data.GameRepositoryImpl
import org.alphakids.app.game.domain.repository.GameRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val gameModule: Module = module {
    single<GameRepository> { GameRepositoryImpl(get<AlphaKidsApiClient>()) }
}
