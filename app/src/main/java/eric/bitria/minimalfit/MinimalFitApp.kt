package eric.bitria.minimalfit

import android.app.Application
import eric.bitria.minimalfit.data.database.DatabaseInitializer
import eric.bitria.minimalfit.di.initKoin
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class MinimalFitApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MinimalFitApp)
        }
        
        val dbInitializer: DatabaseInitializer by inject()
        
        // Initialize mock data on startup
        MainScope().launch {
            dbInitializer.initializeMockData()
        }
    }
}
