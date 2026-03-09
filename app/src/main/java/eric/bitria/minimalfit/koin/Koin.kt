package eric.bitria.minimalfit.koin

import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.repository.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.InMemoryFoodCatalogRepository
import eric.bitria.minimalfit.data.repository.InMemoryJournalRepository
import eric.bitria.minimalfit.data.repository.InMemoryTrackRepository
import eric.bitria.minimalfit.data.repository.JournalRepository
import eric.bitria.minimalfit.data.repository.TrackRepository
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackScreen
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.LocalDate

val dataModule = module {
    singleOf(::FoodDatabase)
    singleOf(::InMemoryJournalRepository) bind JournalRepository::class
    singleOf(::InMemoryFoodCatalogRepository) bind FoodCatalogRepository::class
    singleOf(::InMemoryTrackRepository) bind TrackRepository::class
}

val utilModule = module {
    singleOf(::WeekViewHelper)
}

val viewModels = module {
    viewModelOf(::FoodViewModel)
    viewModel { (date: LocalDate) ->
        DailyLogViewModel(date = date, journal = get())
    }
    viewModelOf(::TrackScreen)
    viewModel { (trackId: String) ->
        TrackDetailViewModel(trackId = trackId, repository = get())
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            dataModule,
            utilModule,
            viewModels,
        )
    }
}
