package eric.bitria.minimalfit.ui.screens.gym.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import eric.bitria.minimalfit.ui.components.gym.cards.GymSessionCard
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymHistoryViewModel
import eric.bitria.minimalfit.util.hourMinute
import eric.bitria.minimalfit.util.shortMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryTab(
    onNavigateToSession: (String?) -> Unit,
    viewModel: GymHistoryViewModel = koinViewModel()
) {
    val sessions by viewModel.pastSessions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        item { Spacer(modifier = Modifier.height(Spacing.xs)) }

        if (sessions.isNotEmpty()) {
            items(sessions, key = { it.session.id }) { session ->
                val sess = session.session
                val setsList = session.sets
                val localStart = sess.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                val dateString = "${localStart.date.shortMonthDay()} • ${localStart.time.hourMinute()}"
                val title = sess.title.ifBlank { "Workout" }
                val mins = sess.durationSeconds / 60
                val secs = sess.durationSeconds % 60
                val duration = "%d:%02d".format(mins, secs)
                val exercisesCount = setsList.map { it.sessionExerciseId }.distinct().size
                val volume = setsList.fold(0f) { acc, s -> acc + (s.weight * s.reps) }

                SwipeToDeleteCard(
                    onDismiss = { viewModel.deleteSession(sess.id) },
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                ) {
                    GymSessionCard(
                        dateString = dateString,
                        title = title,
                        duration = duration,
                        exercisesCount = exercisesCount,
                        setsCount = setsList.size,
                        volume = volume,
                        onClick = { onNavigateToSession(sess.id) }
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "No workouts yet. Start your first session!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
    }
}
