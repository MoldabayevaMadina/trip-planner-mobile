package com.example.tripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tripplanner.data.StatusAction
import com.example.tripplanner.data.StatusChange
import com.example.tripplanner.data.TripStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MAIN_STEPS = listOf(
    TripStatus.PLANNING, TripStatus.READY, TripStatus.IN_PROGRESS, TripStatus.COMPLETED,
)

private val historyTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

@Composable
fun statusColor(status: TripStatus): Color = when (status) {
    TripStatus.PLANNING -> MaterialTheme.colorScheme.secondary
    TripStatus.READY -> MaterialTheme.colorScheme.tertiary
    TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    TripStatus.COMPLETED -> Color(0xFF2E7D32)
    TripStatus.CANCELLED -> MaterialTheme.colorScheme.error
}

@Composable
fun StatusBadge(status: TripStatus, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f), modifier = modifier) {
        Text(
            text = "${status.emoji} ${status.label}",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun StatusStepper(status: TripStatus, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Current status", style = MaterialTheme.typography.labelLarge)
            StatusBadge(status)

            if (status == TripStatus.CANCELLED) {
                Text(
                    "The trip is cancelled. Restore it to continue planning.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                val current = MAIN_STEPS.indexOf(status)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MAIN_STEPS.forEachIndexed { index, step ->
                        val reached = index <= current
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        color = if (reached) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(3.dp),
                                    ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(step.emoji)
                            Text(
                                text = step.label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = if (index == current) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text("Step ${current + 1} of ${MAIN_STEPS.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatusActionRow(action: StatusAction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val text = "${action.target.emoji} ${action.label}"
        val secondary = action.target == TripStatus.CANCELLED || action.target == TripStatus.PLANNING
        if (secondary) {
            OutlinedButton(onClick = onClick, enabled = action.isAllowed, modifier = Modifier.fillMaxWidth()) {
                Text(text)
            }
        } else {
            Button(onClick = onClick, enabled = action.isAllowed, modifier = Modifier.fillMaxWidth()) {
                Text(text)
            }
        }
        action.blockedReason?.let { reason ->
            Text("⛔ $reason", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun HistoryRow(change: StatusChange, modifier: Modifier = Modifier) {
    val time = Instant.ofEpochMilli(change.changedAt)
        .atZone(ZoneId.systemDefault())
        .format(historyTimeFormat)
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(change.to.emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = change.from?.let { "${it.label} → ${change.to.label}" } ?: "Trip created · ${change.to.label}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}