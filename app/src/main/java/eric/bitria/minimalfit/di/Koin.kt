package eric.bitria.minimalfit.di

import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.InMemoryDietRepository
import eric.bitria.minimalfit.data.repository.food.InMemoryFoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.InMemoryJournalRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.data.repository.track.InMemoryTrackRepository
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import eric.bitria.minimalfit.data.repository.track.TrackingLocationRepository
import eric.bitria.minimalfit.data.sensor.ActivitySensor
import eric.bitria.minimalfit.data.sensor.AndroidActivitySensor
import eric.bitria.minimalfit.data.sensor.AndroidLocationSensor
import eric.bitria.minimalfit.data.sensor.LocationSensor
import eric.bitria.minimalfit.data.track.AndroidTrackingManager
import eric.bitria.minimalfit.data.track.TrackingLogic
import eric.bitria.minimalfit.data.track.TrackingManager
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackRecordingViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
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
    singleOf(::InMemoryDietRepository) bind DietRepository::class
    singleOf(::InMemoryTrackRepository) bind TrackRepository::class

    // Sensors
    single { AndroidLocationSensor(androidContext()) } bind LocationSensor::class
    single { AndroidActivitySensor(androidContext()) } bind ActivitySensor::class

    // Repositories
    single {
        TrackingLocationRepository(
            locationSensor = get(),
            activitySensor = get(),
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    } bind LocationRepository::class

    // Tracking Logic (Shared logic)
    singleOf(::TrackingLogic)

    // Tracking Manager (Platform implementation)
    single<TrackingManager> { AndroidTrackingManager(androidContext(), get()) }
}

val utilModule = module {
    singleOf(::WeekViewHelper)
}

val viewModels = module {
    viewModelOf(::FoodViewModel)
    viewModel { (date: LocalDate) ->
        DailyLogViewModel(date = date, journal = get())
    }
    viewModel { (dietId: Int) ->
        DietDetailViewModel(dietId = dietId, dietRepository = get())
    }
    viewModelOf(::TrackViewModel)
    viewModel { (trackId: String) ->
        TrackDetailViewModel(trackId = trackId, repository = get())
    }
    viewModelOf(::TrackRecordingViewModel)
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
