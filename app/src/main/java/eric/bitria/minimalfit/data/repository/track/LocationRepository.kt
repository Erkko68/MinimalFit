package eric.bitria.minimalfit.data.repository.track

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val location: StateFlow<Location?>

    fun startTracking()
    fun stopTracking()
}