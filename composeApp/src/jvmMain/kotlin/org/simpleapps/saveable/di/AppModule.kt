package org.simpleapps.saveable.di
import org.koin.dsl.module
import org.simpleapps.saveable.data.db.DatabaseFactory
import org.simpleapps.saveable.data.repository.MainRepositoryImpl
import org.simpleapps.saveable.domain.command.CommandHandler
import org.simpleapps.saveable.domain.command.CommandParser
import org.simpleapps.saveable.domain.repository.IMainRepository
import org.simpleapps.saveable.domain.usecases.AddCommandUseCase
import org.simpleapps.saveable.domain.usecases.GetListByCategoryUseCase
import org.simpleapps.saveable.ui.MainStateHolder

val appModule = module {

    single { DatabaseFactory.create() }

    single<IMainRepository> { MainRepositoryImpl(get()) }

    factory { AddCommandUseCase(get()) }
    factory { GetListByCategoryUseCase(get()) }

    single { CommandParser() }
    factory { CommandHandler(get(), get()) }

    factory { MainStateHolder(get(), get()) }
}