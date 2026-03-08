package eric.bitria.minimalfit.koin

import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.repository.InMemoryJournalRepository
import eric.bitria.minimalfit.data.repository.JournalRepository
import eric.bitria.minimalfit.ui.viewmodels.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::FoodDatabase)
    singleOf(::InMemoryJournalRepository) bind JournalRepository::class
}

val viewModels = module {
    viewModelOf(::FoodViewModel)
    viewModelOf(::DailyLogViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            dataModule,
            viewModels,
        )
    }
}
