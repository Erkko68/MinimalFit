package eric.bitria.minimalfit.koin

import eric.bitria.minimalfit.data.repository.journal.FoodJournalRepository
import eric.bitria.minimalfit.data.repository.journal.InMemoryFoodJournalRepository
import eric.bitria.minimalfit.data.repository.meal.InMemoryMealRepository
import eric.bitria.minimalfit.data.repository.meal.MealRepository
import eric.bitria.minimalfit.data.repository.tag.InMemoryTagRepository
import eric.bitria.minimalfit.data.repository.tag.TagRepository
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { InMemoryMealRepository() } bind MealRepository::class
    single { InMemoryFoodJournalRepository() } bind FoodJournalRepository::class
    single { InMemoryTagRepository() } bind TagRepository::class
}

val viewModels = module {
    viewModelOf(::FoodViewModel)
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
