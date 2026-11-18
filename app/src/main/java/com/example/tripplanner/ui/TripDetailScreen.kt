package com.example.tripplanner.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripplanner.data.PackingItem
import com.example.tripplanner.data.PackingTemplate
import com.example.tripplanner.data.Place
import com.example.tripplanner.data.PlaceCategory
import com.example.tripplanner.data.StatusAction
import com.example.tripplanner.data.Trip
import com.example.tripplanner.data.TripStatus
import com.example.tripplanner.ui.components.AddItemField
import com.example.tripplanner.ui.components.AppLogo
import com.example.tripplanner.ui.components.EmptyState
import com.example.tripplanner.ui.components.HistoryRow
import com.example.tripplanner.ui.components.PackingRow
import com.example.tripplanner.ui.components.PlaceRow
import com.example.tripplanner.ui.components.StatusActionRow
import com.example.tripplanner.ui.components.StatusBadge
import com.example.tripplanner.ui.components.StatusStepper
import com.example.tripplanner.ui.components.TripCountdown
import com.example.tripplanner.ui.components.VisitedProgress
import com.example.tripplanner.ui.components.datesText
import kotlinx.coroutines.launch

@Composable
fun TripDetailRoute(
    onBack: () -> Unit,
    onEdit: (tripId: Long) -> Unit,
    viewModel: TripDetailViewModel = viewModel(factory = TripDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val trip = state.trip
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isLoading, trip == null) {
        if (!state.isLoading && trip == null) onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    when {
        state.isLoading || trip == null -> Unit
        else -> TripDetailScreen(
            trip = trip,
            statusActions = state.statusActions,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onEdit = { onEdit(trip.id) },
            onDeleteTrip = viewModel::deleteTrip,
            onAddPlace = viewModel::addPlace,
            onTogglePlace = viewModel::togglePlaceVisited,
            onDeletePlace = viewModel::deletePlace,
            onRestorePlace = viewModel::restorePlace,
            onAddPacking = viewModel::addPackingItem,
            onTogglePacked = viewModel::togglePacked,
            onDeletePacking = viewModel::deletePackingItem,
            onRestorePacking = viewModel::restorePackingItem,
            onApplyTemplate = viewModel::applyTemplate,
            onChangeStatus = viewModel::changeStatus,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    statusActions: List<StatusAction>,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteTrip: () -> Unit,
    onAddPlace: (name: String, category: PlaceCategory) -> Unit,
    onTogglePlace: (placeId: Long) -> Unit,
    onDeletePlace: (placeId: Long) -> Unit,
    onRestorePlace: (place: Place) -> Unit,
    onAddPacking: (name: String) -> Unit,
    onTogglePacked: (itemId: Long) -> Unit,
    onDeletePacking: (itemId: Long) -> Unit,
    onRestorePacking: (item: PackingItem) -> Unit,
    onApplyTemplate: (PackingTemplate) -> Unit,
    onChangeStatus: (TripStatus) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun deleteWithUndo(message: String, delete: () -> Unit, restore: () -> Unit) {
        delete()
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) restore()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(trip.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = { shareTrip(context, trip) }) { Text("Share") }
                    TextButton(onClick = { showDeleteDialog = true }) { Text("Delete") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { TripHeader(trip) }
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Places (${trip.places.size})") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Packing (${trip.packedCount}/${trip.packing.size})") },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Status") },
                    )
                }
            }
            when (selectedTab) {
                0 -> placesSection(
                    trip = trip,
                    onAddPlace = onAddPlace,
                    onTogglePlace = onTogglePlace,
                    onDeletePlace = { place ->
                        deleteWithUndo(
                            message = "\"${place.name}\" deleted",
                            delete = { onDeletePlace(place.id) },
                            restore = { onRestorePlace(place) },
                        )
                    },
                )
                1 -> packingSection(
                    trip = trip,
                    onAddPacking = onAddPacking,
                    onTogglePacked = onTogglePacked,
                    onDeletePacking = { item ->
                        deleteWithUndo(
                            message = "\"${item.name}\" removed",
                            delete = { onDeletePacking(item.id) },
                            restore = { onRestorePacking(item) },
                        )
                    },
                    onApplyTemplate = onApplyTemplate,
                )
                else -> statusSection(
                    trip = trip,
                    actions = statusActions,
                    onChangeStatus = onChangeStatus,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete trip?") },
            text = { Text("\"${trip.title}\" with all its places, packing list and history will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteTrip()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }
}

private fun LazyListScope.placesSection(
    trip: Trip,
    onAddPlace: (String, PlaceCategory) -> Unit,
    onTogglePlace: (Long) -> Unit,
    onDeletePlace: (Place) -> Unit,
) {
    item { AddPlaceForm(onAdd = onAddPlace) }
    if (trip.places.isEmpty()) {
        item { EmptyState(message = "No places yet. Add the first one above.") }
    }
    items(trip.places, key = { "place-${it.id}" }) { place ->
        PlaceRow(
            place = place,
            onToggle = { onTogglePlace(place.id) },
            onDelete = { onDeletePlace(place) },
        )
    }
}

private fun LazyListScope.packingSection(
    trip: Trip,
    onAddPacking: (String) -> Unit,
    onTogglePacked: (Long) -> Unit,
    onDeletePacking: (PackingItem) -> Unit,
    onApplyTemplate: (PackingTemplate) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisitedProgress(
                visited = trip.packedCount,
                total = trip.packing.size,
                label = "items packed",
                emptyText = "Nothing to pack yet",
            )
            Text("Quick add from a template:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PackingTemplate.entries.forEach { template ->
                    AssistChip(
                        onClick = { onApplyTemplate(template) },
                        label = { Text("${template.emoji} ${template.label}") },
                    )
                }
            }
            AddItemField(label = "New item", onAdd = onAddPacking)
        }
    }
    if (trip.packing.isEmpty()) {
        item { EmptyState(message = "Packing list is empty. Pick a template above.") }
    }
    items(trip.packing.sortedBy { it.packed }, key = { "pack-${it.id}" }) { item ->
        PackingRow(
            item = item,
            onToggle = { onTogglePacked(item.id) },
            onDelete = { onDeletePacking(item) },
        )
    }
}

private fun LazyListScope.statusSection(
    trip: Trip,
    actions: List<StatusAction>,
    onChangeStatus: (TripStatus) -> Unit,
) {
    item { StatusStepper(trip.status) }
    item { Text("Next step", style = MaterialTheme.typography.titleMedium) }
    if (actions.isEmpty()) {
        item { EmptyState(message = "The trip is completed. No more steps 🎉") }
    }
    items(actions, key = { "action-${it.target.name}" }) { action ->
        StatusActionRow(action = action, onClick = { onChangeStatus(action.target) })
    }
    item { Text("History", style = MaterialTheme.typography.titleMedium) }
    if (trip.history.isEmpty()) {
        item { Text("No changes yet", style = MaterialTheme.typography.bodySmall) }
    }
    items(trip.history, key = { "history-${it.id}" }) { change ->
        HistoryRow(change)
    }
}

@Composable
fun TripHeader(trip: Trip, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 64.dp)
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = listOf(trip.city, trip.country).filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text("${trip.datesText()} · ${trip.daysCount} days", style = MaterialTheme.typography.bodyMedium)
                StatusBadge(trip.status)
                TripCountdown(trip)
                if (trip.notes.isNotBlank()) {
                    Text("📝 ${trip.notes}", style = MaterialTheme.typography.bodySmall)
                }
                VisitedProgress(visited = trip.visitedCount, total = trip.places.size)
            }
        }
    }
}

@Composable
fun AddPlaceForm(onAdd: (String, PlaceCategory) -> Unit, modifier: Modifier = Modifier) {
    var category by rememberSaveable { mutableStateOf(PlaceCategory.SIGHT) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddItemField(label = "New place", onAdd = { name -> onAdd(name, category) })
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlaceCategory.entries.forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text("${option.emoji} ${option.label}") },
                )
            }
        }
    }
}

private fun Trip.toShareText(): String = buildString {
    appendLine("🧭 $title")
    appendLine("📍 ${listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")}")
    appendLine("📅 ${datesText()} ($daysCount days)")
    appendLine("${status.emoji} ${status.label}")
    if (notes.isNotBlank()) appendLine("📝 $notes")
    if (places.isNotEmpty()) {
        appendLine()
        appendLine("Places:")
        places.forEach { appendLine("${if (it.visited) "✅" else "▫️"} ${it.category.emoji} ${it.name}") }
    }
    val toPack = packing.filterNot { it.packed }
    if (toPack.isNotEmpty()) {
        appendLine()
        appendLine("Still to pack:")
        toPack.forEach { appendLine("• ${it.name}") }
    }
}

private fun shareTrip(context: Context, trip: Trip) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, trip.title)
        putExtra(Intent.EXTRA_TEXT, trip.toShareText())
    }
    context.startActivity(Intent.createChooser(send, "Share trip plan"))
}