package com.example.loophabittracker.presentation.add_edit_habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val targetType by viewModel.targetType.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val notes by viewModel.notes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                            .clickable { /* TBD: Add Color Picker Dropdown */ }
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
                
                OutlinedTextField(
                    value = penalty,
                    onValueChange = { viewModel.updateField("penalty", it) },
                    label = { Text("Penalty Value") },
                    placeholder = { Text("Optional penalty deduction if goal missed") },
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
