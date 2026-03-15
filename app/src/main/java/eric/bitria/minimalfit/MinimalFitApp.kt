package eric.bitria.minimalfit

import android.app.Application
import eric.bitria.minimalfit.di.initKoin
import org.koin.android.ext.koin.androidContext

class MinimalFitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@MinimalFitApp)
        }
    }
}
