package com.example.loophabittracker.presentation.add_edit_habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: AddEditHabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val name by viewModel.habitName.collectAsState()
    val color by viewModel.habitColor.collectAsState()
    val isMeasurable by viewModel.isMeasurable.collectAsState()
    val question by viewModel.question.collectAsState()
    val unit by viewModel.unit.collectAsState()
    val target by viewModel.target.collectAsState()
    val frequency by viewModel.frequency.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val targetType by viewModel.targetType.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showColorPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to completely delete '${name}'? All history will be permanently lost.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteHabit(onNavigateBack) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = { 
                viewModel.onColorChanged(it.toArgb())
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (name.isNotEmpty()) "Edit Habit" else "Create habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isExistingHabit) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Habit")
                        }
                    }
                    TextButton(onClick = { viewModel.saveHabit(onComplete = onNavigateBack) }) {
                        Text("SAVE", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateField("name", it) },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Run") },
                    modifier = Modifier.weight(1f)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Color", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(color), MaterialTheme.shapes.small)
                            .clickable { showColorPicker = true }
                    )
                }
            }

            OutlinedTextField(
                value = question,
                onValueChange = { viewModel.updateField("question", it) },
                label = { Text("Question") },
                placeholder = { Text("e.g. How many miles did you run today?") },
                modifier = Modifier.fillMaxWidth()
            )

            // Habit Type Toggle
            Text("Habit Type", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isMeasurable,
                        onClick = { viewModel.updateBoolean("isMeasurable", false) }
                    )
                    Text("Yes/No", modifier = Modifier.clickable { viewModel.updateBoolean("isMeasurable", false) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isMeasurable,
                        onClick = { viewModel.updateBoolean("isMeasurable", true) }
                    )
                    Text("Measurable", modifier = Modifier.clickable { viewModel.updateBoolean("isMeasurable", true) })
                }
            }

            if (isMeasurable) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { viewModel.updateField("unit", it) },
                    label = { Text("Unit") },
                    placeholder = { Text("e.g. miles") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { viewModel.updateField("target", it) },
                        label = { Text("Target") },
                        placeholder = { Text("e.g. 15") },
                        modifier = Modifier.weight(1f)
                    )

                    SimpleDropdown(
                        label = "Frequency",
                        options = listOf("Every day", "Every week", "Every month"),
                        selectedValue = frequency,
                        onValueChange = { viewModel.updateField("frequency", it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                SimpleDropdown(
                    label = "Target Type",
                    options = listOf("At least", "At most", "Exactly"),
                    selectedValue = targetType,
                    onValueChange = { viewModel.updateField("targetType", it) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SimpleDropdown(
                    label = "Frequency",
                    options = listOf("Every day", "Every week", "Every month"),
                    selectedValue = frequency,
                    onValueChange = { viewModel.updateField("frequency", it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Scheduling Selector
            if (frequency == "Every week") {
                Text("Select active days:", style = MaterialTheme.typography.bodyMedium)
                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    daysOfWeek.forEachIndexed { index, day ->
                        val isSelected = selectedDays.contains(index + 1)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { viewModel.toggleDaySelection(index + 1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.take(1), color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            } else if (frequency == "Every month") {
                Text("Select active dates:", style = MaterialTheme.typography.bodyMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items((1..31).toList()) { date ->
                        val isSelected = selectedDays.contains(date)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { viewModel.toggleDaySelection(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(date.toString(), style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Universal Fields
            OutlinedTextField(
                value = penalty,
                onValueChange = { viewModel.updateField("penalty", it) },
                label = { Text("Penalty Value") },
                placeholder = { Text("Optional penalty deduction if goal missed") },
                modifier = Modifier.fillMaxWidth()
            )

            SimpleDropdown(
                label = "Reminder",
                options = listOf("Off", "Custom Time"),
                selectedValue = reminder,
                onValueChange = { viewModel.updateField("reminder", it) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateField("notes", it) },
                label = { Text("Notes") },
                placeholder = { Text("(Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val presetColors = listOf(
        Color(0xFFF44336), // Red
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722)  // Deep Orange
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a color") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(presetColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
