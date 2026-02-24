package eric.bitria.minimalfit.koin

import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val viewModels = module {
    viewModelOf(::FoodViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            viewModels,
        )
    }
}
