package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.database.dao.SyncQueueDao
import eric.bitria.minimalfit.data.remote.auth.AuthRepository

class SyncOrchestrator(
    private val gymSync: GymSyncService,
    private val foodSync: FoodSyncService,
    private val trackSync: TrackSyncService,
    private val authRepository: AuthRepository,
    private val syncQueueDao: SyncQueueDao,
    private val log: SyncLogStore
) {
    companion object {
        private const val TAG = "SyncOrchestrator"
    }

    suspend fun syncAll(): Result<Unit> = runCatching {
        val uid = authRepository.currentUser.value?.uid
        if (uid == null) {
            log.d(TAG, "syncAll skipped — no authenticated user")
            return@runCatching
        }

        log.d(TAG, "Starting full sync for user $uid")

        gymSync.syncGlobalExercises().onFailure { log.w(TAG, "Global exercises sync failed: ${it.message}") }
        foodSync.syncGlobalIngredients().onFailure { log.w(TAG, "Global ingredients sync failed: ${it.message}") }
        gymSync.backupUserExercises().onFailure { log.w(TAG, "User exercises backup failed: ${it.message}") }
        gymSync.backupRoutines().onFailure { log.w(TAG, "Routines backup failed: ${it.message}") }
        gymSync.backupSessions().onFailure { log.w(TAG, "Sessions backup failed: ${it.message}") }
        foodSync.backupUserIngredients().onFailure { log.w(TAG, "User ingredients backup failed: ${it.message}") }
        foodSync.backupMeals().onFailure { log.w(TAG, "Meals backup failed: ${it.message}") }
        foodSync.backupDiets().onFailure { log.w(TAG, "Diets backup failed: ${it.message}") }
        foodSync.backupMealLogs().onFailure { log.w(TAG, "Meal logs backup failed: ${it.message}") }
        trackSync.backupTracks().onFailure { log.w(TAG, "Tracks backup failed: ${it.message}") }

        log.d(TAG, "Full sync completed for user $uid")
        syncQueueDao.clearAll()
    }

    suspend fun restoreAll(): Result<Unit> = runCatching {
        val uid = authRepository.currentUser.value?.uid
        if (uid == null) {
            log.d(TAG, "restoreAll skipped — no authenticated user")
            return@runCatching
        }

        log.d(TAG, "Starting full restore for user $uid")

        gymSync.syncGlobalExercises().onFailure { log.w(TAG, "Global exercises restore failed: ${it.message}") }
        foodSync.syncGlobalIngredients().onFailure { log.w(TAG, "Global ingredients restore failed: ${it.message}") }
        gymSync.restoreUserExercises().onFailure { log.w(TAG, "User exercises restore failed: ${it.message}") }
        gymSync.restoreRoutines().onFailure { log.w(TAG, "Routines restore failed: ${it.message}") }
        gymSync.restoreSessions().onFailure { log.w(TAG, "Sessions restore failed: ${it.message}") }
        foodSync.restoreUserIngredients().onFailure { log.w(TAG, "User ingredients restore failed: ${it.message}") }
        foodSync.restoreMeals().onFailure { log.w(TAG, "Meals restore failed: ${it.message}") }
        foodSync.restoreDiets().onFailure { log.w(TAG, "Diets restore failed: ${it.message}") }
        foodSync.restoreMealLogs().onFailure { log.w(TAG, "Meal logs restore failed: ${it.message}") }
        trackSync.restoreTracks().onFailure { log.w(TAG, "Tracks restore failed: ${it.message}") }

        log.d(TAG, "Full restore completed for user $uid")
    }
}
