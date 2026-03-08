package eric.bitria.minimalfit.koin

import eric.bitria.minimalfit.data.repository.JournalRepository
import eric.bitria.minimalfit.ui.viewmodels.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JournalRepository)
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
