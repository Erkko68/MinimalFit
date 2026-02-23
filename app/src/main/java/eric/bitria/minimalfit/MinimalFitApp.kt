package eric.bitria.minimalfit

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MinimalFitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MinimalFitApp)
        }
    }
}
