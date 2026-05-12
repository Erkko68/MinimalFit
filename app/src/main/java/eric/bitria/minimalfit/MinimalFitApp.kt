package eric.bitria.minimalfit

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.sync.SyncOrchestrator
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import eric.bitria.minimalfit.di.initKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class MinimalFitApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MinimalFitApp)
        }

        // Observe auth state and trigger sync when user logs in
        val authRepository: AuthRepository by inject()
        val syncOrchestrator: SyncOrchestrator by inject()
        val syncScheduler: SyncScheduler by inject()

        appScope.launch {
            authRepository.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    if (uid != null) {
                        syncOrchestrator.restoreAll()
                    }
                }
        }

        // Flush pending queue when app goes to background
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                appScope.launch { syncScheduler.flushNow() }
            }
        })
    }
}
