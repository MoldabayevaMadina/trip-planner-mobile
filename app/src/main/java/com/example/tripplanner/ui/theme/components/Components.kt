package com.example.tripplanner.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tripplanner.R
import com.example.tripplanner.data.PackingItem
import com.example.tripplanner.data.Place
import com.example.tripplanner.data.Trip
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val fullDate = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)


fun Trip.datesText(): String = "${startDate.format(shortDate)} – ${endDate.format(fullDate)}"

@Composable
fun TripCard(trip: Trip, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CityBadge(city = trip.city)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    TripStatus(trip)
                }
                Text(
                    text = "${trip.city}, ${trip.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = "${trip.datesText()} · ${trip.daysCount} days", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                VisitedProgress(visited = trip.visitedCount, total = trip.places.size)
            }
        }
    }
}

@Composable
fun TripStatus(trip: Trip, modifier: Modifier = Modifier) {
    Text(
        text = trip.statusText(),
        style = MaterialTheme.typography.labelMedium,
        color = if (trip.isPast()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
fun CityBadge(city: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = city.take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun VisitedProgress(
    visited: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String = "places visited",
    emptyText: String = "No places yet",
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else visited.toFloat() / total },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = if (total == 0) emptyText else "$visited / $total $label",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun PlaceRow(place: Place, onToggle: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = place.visited, onCheckedChange = { onToggle() })
            Text(text = place.category.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (place.visited) TextDecoration.LineThrough else null,
                )
                Text(
                    text = place.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onDelete) { Text("✕") }
        }
    }
}

@Composable
fun PackingRow(item: PackingItem, onToggle: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.packed, onCheckedChange = { onToggle() })
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.packed) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDelete) { Text("✕") }
        }
    }
}

@Composable
fun AddItemField(label: String, onAdd: (String) -> Unit, modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(
            enabled = text.isNotBlank(),
            onClick = {
                onAdd(text.trim())
                text = ""
            },
        ) { Text("Add") }
    }
}

@Composable
fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Trip Planner logo",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppLogo(size = 72.dp)
        Spacer(Modifier.height(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}