package eric.bitria.minimalfit.di

import androidx.room.Room
import eric.bitria.minimalfit.data.database.AppDatabase
import eric.bitria.minimalfit.data.database.DatabaseInitializer
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.DefaultDietRepository
import eric.bitria.minimalfit.data.repository.food.DefaultFoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.DefaultJournalRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.data.repository.track.DefaultTrackRepository
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
import eric.bitria.minimalfit.ui.viewmodels.ProfileViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.MealDetailViewModel
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
import kotlinx.datetime.LocalDate

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "minimalfit.db"
        ).build()
    }

    single { get<AppDatabase>().trackDao() }
    single { get<AppDatabase>().mealDao() }
    single { get<AppDatabase>().ingredientDao() }
    single { get<AppDatabase>().dietDao() }
    single { get<AppDatabase>().mealLogDao() }

    singleOf(::DatabaseInitializer)
    
    // Food Catalog Repository
    single<FoodCatalogRepository> {
        DefaultFoodCatalogRepository(
            mealDao = get(),
            ingredientDao = get()
        )
    }

    // Journal Repository
    single<JournalRepository> {
        DefaultJournalRepository(
            mealLogDao = get(),
            mealDao = get(),
            foodCatalog = get()
        )
    }

    // Diet Repository
    single<DietRepository> {
        DefaultDietRepository(
            dietDao = get(),
            mealDao = get(),
            foodCatalog = get()
        )
    }

    singleOf(::DefaultTrackRepository) bind TrackRepository::class

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

val viewModels = module {
    viewModelOf(::FoodViewModel)
    viewModel { (date: LocalDate) ->
        DailyLogViewModel(date = date, journal = get(), foodCatalog = get())
    }
    viewModel { (dietId: String) ->
        DietDetailViewModel(dietId = dietId, dietRepository = get(), foodCatalog = get())
    }
    viewModel { (mealId: String) ->
        MealDetailViewModel(mealId = mealId, foodCatalog = get())
    }
    viewModelOf(::TrackViewModel)
    viewModel { (trackId: String) ->
        TrackDetailViewModel(trackId = trackId, repository = get())
    }
    viewModelOf(::TrackRecordingViewModel)
    viewModelOf(::ProfileViewModel)
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
