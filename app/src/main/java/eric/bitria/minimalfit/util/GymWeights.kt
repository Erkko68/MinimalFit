package eric.bitria.minimalfit.util

import kotlin.math.abs

/**
 * Non-linear list of weight values (kg) that mirrors how gym equipment is
 * actually loaded:
 *
 *   0 – 10 kg   → 0.5 kg steps  (dumbbells / warm-up)
 *  10 – 40 kg   → 2.5 kg steps  (barbell + small plates)
 *  40 – 100 kg  → 5 kg steps    (barbell + medium plates)
 * 100 – 200 kg  → 10 kg steps   (barbell + heavy plates)
 */
val GYM_WEIGHT_VALUES: List<Float> by lazy {
    buildList {
        // 0.0, 0.5, 1.0 … 10.0
        var w = 0f
        while (w <= 10f) {
            add(w)
            w = (w * 10 + 5).toInt() / 10f  // integer arithmetic avoids float drift
        }
        // 12.5, 15.0 … 40.0
        w = 12.5f
        while (w <= 40f) {
            add(w)
            w += 2.5f
        }
        // 45, 50 … 100
        w = 45f
        while (w <= 100f) {
            add(w)
            w += 5f
        }
        // 110, 120 … 200
        w = 110f
        while (w <= 200f) {
            add(w)
            w += 10f
        }
    }
}

/** Nearest index in [GYM_WEIGHT_VALUES] for [weight]. */
fun weightToIndex(weight: Float): Int =
    GYM_WEIGHT_VALUES.indices.minByOrNull { abs(GYM_WEIGHT_VALUES[it] - weight) } ?: 0

/** Format a weight value for display: whole numbers without decimals, others with one. */
fun formatWeight(w: Float): String =
    if (w == w.toLong().toFloat()) w.toLong().toString() else "%.1f".format(w)
