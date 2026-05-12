package eric.bitria.minimalfit.data.remote.sync

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SyncLogStore {
    private val _entries = MutableStateFlow<List<SyncLogEntry>>(emptyList())
    val entries: StateFlow<List<SyncLogEntry>> = _entries

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        append(tag, message, SyncLogEntry.Level.DEBUG)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        append(tag, message, SyncLogEntry.Level.WARN)
    }

    fun clear() { _entries.value = emptyList() }

    private fun append(tag: String, message: String, level: SyncLogEntry.Level) {
        _entries.update { (it + SyncLogEntry(tag = tag, message = message, level = level)).takeLast(300) }
    }
}
