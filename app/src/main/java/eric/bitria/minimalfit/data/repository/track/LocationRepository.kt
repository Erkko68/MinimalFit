package eric.bitria.minimalfit.data.repository.track

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val location: StateFlow<Location?>
    val isTracking: Boolean
    val isGpsEnabled: Flow<Boolean>
    val hasPermission: Boolean

    fun startTracking()
    fun stopTracking()
}