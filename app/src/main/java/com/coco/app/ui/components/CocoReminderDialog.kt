package com.coco.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.coco.app.R
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoOnBrown
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocoReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val now = remember { Calendar.getInstance() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = now.timeInMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )

    var isSelectingDate by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphic(
                    shape = RoundedCornerShape(28.dp),
                    surface = CocoCream,
                    elevation = 6.dp
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⏰ Programar idea",
                    style = MaterialTheme.typography.titleLarge,
                    color = CocoBrownDark
                )
                Spacer(Modifier.height(16.dp))

                // Pestañas de Fecha / Hora
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(CocoBrown.copy(alpha = 0.12f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabMod = { selected: Boolean ->
                        Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (selected) CocoBrownDark else Color.Transparent)
                            .padding(vertical = 8.dp)
                    }
                    Box(
                        modifier = tabMod(isSelectingDate).clickable { isSelectingDate = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📅 Fecha",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelectingDate) CocoOnBrown else CocoBrownDark
                        )
                    }
                    Box(
                        modifier = tabMod(!isSelectingDate).clickable { isSelectingDate = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏰ Hora",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (!isSelectingDate) CocoOnBrown else CocoBrownDark
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = isSelectingDate,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "PickerTab"
                ) { showDate ->
                    if (showDate) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.Transparent,
                                selectedDayContainerColor = CocoGreen,
                                selectedDayContentColor = Color.White,
                                todayDateBorderColor = CocoBrown,
                                todayContentColor = CocoBrownDark
                            ),
                            showModeToggle = false
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = CocoBrown.copy(alpha = 0.08f),
                                    selectorColor = CocoGreen,
                                    containerColor = Color.Transparent,
                                    clockDialSelectedContentColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                            color = CocoBrown
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CocoGreen)
                            .clickable {
                                val selectedDate = datePickerState.selectedDateMillis ?: now.timeInMillis
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onConfirm(cal.timeInMillis)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
