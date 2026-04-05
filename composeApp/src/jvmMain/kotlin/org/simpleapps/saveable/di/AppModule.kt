package org.simpleapps.saveable.di
import org.koin.dsl.module
import org.simpleapps.saveable.data.db.DatabaseFactory
import org.simpleapps.saveable.data.repository.MainRepositoryImpl
import org.simpleapps.saveable.domain.command.CommandHandler
import org.simpleapps.saveable.domain.command.CommandParser
import org.simpleapps.saveable.domain.repository.IMainRepository
import org.simpleapps.saveable.domain.usecases.AddCategoryUseCase
import org.simpleapps.saveable.domain.usecases.AddItemUseCase
import org.simpleapps.saveable.domain.usecases.DeleteItemUseCase
import org.simpleapps.saveable.domain.usecases.EditItemUseCase
import org.simpleapps.saveable.domain.usecases.GetCategoriesUseCase
import org.simpleapps.saveable.domain.usecases.GetListByCategoryUseCase
import org.simpleapps.saveable.domain.usecases.TranslateUseCase
import org.simpleapps.saveable.ui.MainStateHolder

val appModule = module {

    single { DatabaseFactory.create() }

    single<IMainRepository> { MainRepositoryImpl(get()) }

    factory { AddItemUseCase(get()) }
    factory { EditItemUseCase(get()) }
    factory { DeleteItemUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { GetListByCategoryUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { TranslateUseCase() }

    single { CommandParser() }
    factory { CommandHandler(get(), get(), get(), get(), get(), get()) }

    factory { MainStateHolder(get(), get(), get()) }
}