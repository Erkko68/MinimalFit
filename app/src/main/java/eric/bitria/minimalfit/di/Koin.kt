package eric.bitria.minimalfit.di

import androidx.room.Room
import eric.bitria.minimalfit.data.database.AppDatabase
import eric.bitria.minimalfit.data.database.DatabaseInitializer
import eric.bitria.minimalfit.data.repository.food.DefaultDietRepository
import eric.bitria.minimalfit.data.repository.food.DefaultFoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.DefaultJournalRepository
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.data.repository.track.DefaultTrackRepository
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import eric.bitria.minimalfit.data.repository.track.TrackingLocationRepository
import eric.bitria.minimalfit.data.repository.gym.DefaultExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.DefaultSetRepository
import eric.bitria.minimalfit.data.repository.gym.DefaultSessionRepository
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.gym.AndroidGymSessionManager
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.gym.GymTrackingLogic
import eric.bitria.minimalfit.data.sensor.ActivitySensor
import eric.bitria.minimalfit.data.sensor.AndroidActivitySensor
import eric.bitria.minimalfit.data.sensor.AndroidLocationSensor
import eric.bitria.minimalfit.data.sensor.LocationSensor
import eric.bitria.minimalfit.data.track.AndroidTrackingManager
import eric.bitria.minimalfit.data.track.TrackingLogic
import eric.bitria.minimalfit.data.track.TrackingManager
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.food.MealDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.gym.GymHomeViewModel
import eric.bitria.minimalfit.ui.viewmodels.gym.GymSessionViewModel
import eric.bitria.minimalfit.ui.viewmodels.gym.ExerciseProgressionViewModel
import eric.bitria.minimalfit.ui.viewmodels.profile.ProfileViewModel
import eric.bitria.minimalfit.ui.viewmodels.profile.card.CalorieViewModel
import eric.bitria.minimalfit.ui.viewmodels.profile.card.GymViewModel
import eric.bitria.minimalfit.ui.viewmodels.profile.card.WaterViewModel
import eric.bitria.minimalfit.ui.viewmodels.settings.SettingsViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackDetailViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackRecordingViewModel
import eric.bitria.minimalfit.ui.viewmodels.track.TrackViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.LocalDate
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "minimalfit.db"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)
            .build()
    }

    single { get<AppDatabase>().trackDao() }
    single { get<AppDatabase>().mealDao() }
    single { get<AppDatabase>().ingredientDao() }
    single { get<AppDatabase>().dietDao() }
    single { get<AppDatabase>().mealLogDao() }
    single { get<AppDatabase>().exerciseDao() }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().setDao() }

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

    // Gym Repositories
    single<SessionRepository> { DefaultSessionRepository(sessionDao = get(), setDao = get()) }
    single<ExerciseRepository> { DefaultExerciseRepository(exerciseDao = get()) }
    single<SetRepository> { DefaultSetRepository(setDao = get()) }
    singleOf(::GymTrackingLogic)
    single<GymSessionManager> { AndroidGymSessionManager(androidContext(), get()) }

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

    // Setting

    viewModelOf(::SettingsViewModel)

    // Profile

    viewModelOf(::ProfileViewModel)
    viewModelOf(::WaterViewModel)
    viewModelOf(::GymViewModel)
    viewModelOf(::CalorieViewModel)
    viewModelOf(::TrackViewModel)

    // Gym
    viewModelOf(::GymHomeViewModel)
    viewModel { (sessionId: String?) ->
        GymSessionViewModel(
            sessionId = sessionId,
            sessionRepository = get(),
            exerciseRepository = get(),
            setRepository = get(),
            gymSessionManager = get()
        )
    }
    viewModel { (exerciseId: String) ->
        ExerciseProgressionViewModel(
            exerciseId = exerciseId,
            exerciseRepository = get(),
            setRepository = get()
        )
    }
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
