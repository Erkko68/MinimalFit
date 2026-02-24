package eric.bitria.minimalfit

import android.app.Application
import eric.bitria.minimalfit.koin.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MinimalFitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@MinimalFitApp)
        }
    }
}
