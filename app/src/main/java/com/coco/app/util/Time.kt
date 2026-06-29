package com.coco.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coco.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tiempo relativo localizado para las tarjetas del historial. */
@Composable
fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - epochMillis) / 60_000L
    return when {
        minutes < 1 -> stringResource(R.string.time_now)
        minutes < 60 -> stringResource(R.string.time_min_ago, minutes.toInt())
        minutes < 1_440 -> stringResource(R.string.time_hours_ago, (minutes / 60).toInt())
        else -> SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
    }
}
