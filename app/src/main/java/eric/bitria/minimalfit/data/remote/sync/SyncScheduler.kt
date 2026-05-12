package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.database.dao.SyncQueueDao
import eric.bitria.minimalfit.data.database.entity.SyncQueueEntry
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.repository.user.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncScheduler(
    private val syncQueueDao: SyncQueueDao,
    private val gymSyncService: GymSyncService,
    private val foodSyncService: FoodSyncService,
    private val trackSyncService: TrackSyncService,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val log: SyncLogStore
) {
    companion object {
        private const val TAG = "SyncScheduler"
        private const val DEBOUNCE_MS = 5_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var debounceJob: Job? = null

    private val autoSyncEnabled: StateFlow<Boolean> = userPreferencesRepository.isAutoSyncEnabled
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun enqueue(entityType: String, entityId: String, action: String) {
        if (!autoSyncEnabled.value) return
        scope.launch {
            syncQueueDao.enqueue(SyncQueueEntry(entityType = entityType, entityId = entityId, action = action))
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(DEBOUNCE_MS)
                flush()
            }
        }
    }

    suspend fun flush() {
        val uid = authRepository.currentUser.value?.uid ?: run {
            log.d(TAG, "flush skipped — no authenticated user")
            return
        }
        val entries = syncQueueDao.getAll()
        if (entries.isEmpty()) return

        val deduped = entries
            .groupBy { it.entityType to it.entityId }
            .mapValues { (_, list) -> list.last() }
            .values

        log.d(TAG, "Flushing ${deduped.size} entr${if (deduped.size == 1) "y" else "ies"} (from ${entries.size} queued)")

        val processedIds = mutableListOf<Long>()
        for (entry in deduped) {
            runCatching {
                when (entry.action) {
                    "upsert" -> handleUpsert(uid, entry.entityType, entry.entityId)
                    "delete" -> handleDelete(uid, entry.entityType, entry.entityId)
                }
            }.onSuccess {
                processedIds += entries
                    .filter { it.entityType == entry.entityType && it.entityId == entry.entityId }
                    .map { it.id }
            }.onFailure {
                log.w(TAG, "Failed to sync ${entry.entityType}/${entry.entityId}: ${it.message}")
            }
        }

        if (processedIds.isNotEmpty()) syncQueueDao.deleteByIds(processedIds)
    }

    suspend fun flushNow() {
        debounceJob?.cancel()
        flush()
    }

    private suspend fun handleUpsert(uid: String, entityType: String, entityId: String) {
        when (entityType) {
            "exercise"   -> gymSyncService.uploadSingleExercise(uid, entityId)
            "routine"    -> gymSyncService.uploadSingleRoutine(uid, entityId)
            "session"    -> gymSyncService.uploadSingleSession(uid, entityId)
            "ingredient" -> foodSyncService.uploadSingleIngredient(uid, entityId)
            "meal"       -> foodSyncService.uploadSingleMeal(uid, entityId)
            "diet"       -> foodSyncService.uploadSingleDiet(uid, entityId)
            "meal_log"   -> foodSyncService.uploadSingleMealLog(uid, entityId)
            "track"      -> trackSyncService.uploadSingleTrack(uid, entityId)
            else -> log.w(TAG, "Unknown entity type for upsert: $entityType")
        }
    }

    private suspend fun handleDelete(uid: String, entityType: String, entityId: String) {
        when (entityType) {
            "exercise"   -> gymSyncService.deleteSingleExercise(uid, entityId)
            "routine"    -> gymSyncService.deleteSingleRoutine(uid, entityId)
            "session"    -> gymSyncService.deleteSingleSession(uid, entityId)
            "ingredient" -> foodSyncService.deleteSingleIngredient(uid, entityId)
            "meal"       -> foodSyncService.deleteSingleMeal(uid, entityId)
            "diet"       -> foodSyncService.deleteSingleDiet(uid, entityId)
            "meal_log"   -> foodSyncService.deleteSingleMealLog(uid, entityId)
            "track"      -> trackSyncService.deleteSingleTrack(uid, entityId)
            else -> log.w(TAG, "Unknown entity type for delete: $entityType")
        }
    }
}
