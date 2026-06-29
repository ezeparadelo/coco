package com.coco.app.ui.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.coco.app.R
import com.coco.app.ui.theme.CocoBrown
import com.coco.app.ui.theme.CocoBrownDark
import com.coco.app.ui.theme.CocoCream
import com.coco.app.ui.theme.CocoGreen
import com.coco.app.ui.theme.CocoInk
import com.coco.app.ui.theme.CocoOnBrown

@Composable
fun SettingsDialog(
    startInHistory: Boolean,
    onStartInHistoryChange: (Boolean) -> Unit,
    fastMode: Boolean,
    onFastModeChange: (Boolean) -> Unit,
    enterToSubmit: Boolean,
    onEnterToSubmitChange: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    deletedCount: Int = 0,
    onOpenTrash: () -> Unit = {},
    easterEggTaps: Int = 0,
    onIncrementEasterEggTaps: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = CocoCream,
            tonalElevation = 0.dp,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CocoBrownDark,
                )
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.start_in_history),
                            style = MaterialTheme.typography.titleMedium,
                            color = CocoInk,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.start_in_history_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoBrown,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = startInHistory,
                        onCheckedChange = onStartInHistoryChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CocoOnBrown,
                            checkedTrackColor = CocoGreen,
                            uncheckedThumbColor = CocoCream,
                            uncheckedTrackColor = CocoBrown,
                        ),
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.fast_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = CocoInk,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.fast_mode_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoBrown,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = fastMode,
                        onCheckedChange = onFastModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CocoOnBrown,
                            checkedTrackColor = CocoGreen,
                            uncheckedThumbColor = CocoCream,
                            uncheckedTrackColor = CocoBrown,
                        ),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enter_to_submit),
                            style = MaterialTheme.typography.titleMedium,
                            color = CocoInk,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.enter_to_submit_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoBrown,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = enterToSubmit,
                        onCheckedChange = onEnterToSubmitChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CocoOnBrown,
                            checkedTrackColor = CocoGreen,
                            uncheckedThumbColor = CocoCream,
                            uncheckedTrackColor = CocoBrown,
                        ),
                    )
                }

                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(CocoBrown.copy(alpha = 0.15f)))
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.backup_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = CocoInk,
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(CocoBrown.copy(alpha = 0.1f))
                            .clickable { onExportBackup() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.export_backup), style = MaterialTheme.typography.labelMedium, color = CocoBrownDark)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(CocoBrown.copy(alpha = 0.1f))
                            .clickable { onImportBackup() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.import_backup), style = MaterialTheme.typography.labelMedium, color = CocoBrownDark)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Coco v1.0.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = CocoBrownDark,
                    )
                    Spacer(Modifier.height(2.dp))
                    val madeByString = stringResource(R.string.credits_made_by)
                    AnimatedContent(
                        targetState = easterEggTaps >= 10,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "easter_egg"
                    ) { isEgg ->
                        Text(
                            text = if (isEgg) "made by @charlie with 💛" else madeByString,
                            style = MaterialTheme.typography.bodySmall,
                            color = CocoBrown,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onIncrementEasterEggTaps()
                                if (easterEggTaps + 1 == 10) {
                                    Toast.makeText(context, "Actually...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
