package com.example.loophabittracker.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    habitId: Int,
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    LaunchedEffect(habitId) {
        viewModel.loadStatistics(habitId)
    }

    val habitColor = uiState.habit?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.habit?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White) }
                    IconButton(onClick = { /* TODO: More */ }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = habitColor, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

            // Month Selector Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month", tint = habitColor)
                    }
                    Text(
                        text = "$monthLabel ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = habitColor
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month", tint = habitColor)
                    }
                }
                TextButton(onClick = { viewModel.resetToCurrentMonth() }) {
                    Text("TODAY", color = habitColor, fontWeight = FontWeight.Bold)
                }
            }
            Divider(modifier = Modifier.padding(bottom = 8.dp))

            // Section 1: Monthly Summary
            SectionTitle("Monthly Overview", habitColor)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStat("Active Days", "${uiState.totalDaysInMonth}", habitColor)
                OverviewStat("Failures", "${uiState.totalFailures}", MaterialTheme.colorScheme.onSurfaceVariant)
                OverviewStat("Penalty", "-${String.format("%.0f", uiState.totalPenalty)}", Color.Red)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Section 2: Full Calendar Grid
            SectionTitle("Calendar Breakdown", habitColor)
            
            val blankDays = currentMonth.atDay(1).dayOfWeek.value - 1
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(blankDays) {
                    Box(modifier = Modifier.aspectRatio(1f)) // Empty padding
                }
                items(uiState.calendarData) { dayData ->
                    CalendarDayBox(dayData, habitColor, uiState.habit?.isMeasurable == true, uiState.habit?.unit ?: "")
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    )
}

@Composable
fun OverviewStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = valueColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CalendarDayBox(data: CalendarDayData, activeColor: Color, isMeasurable: Boolean, unit: String) {
    val dayStr = data.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
    val bgColor = if (data.isCompleted) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)
    val borderColor = if (data.isCompleted) activeColor else MaterialTheme.colorScheme.outlineVariant
    
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(2.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Weekday + Date
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dayStr, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = data.date.dayOfMonth.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            // Middle: Value / Marker
            if (data.isFuture) {
                Text(text = "-", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                if (isMeasurable) {
                    val displayVal = if (data.value != null) {
                        if (data.value % 1.0f == 0f) data.value.toInt().toString() else data.value.toString()
                    } else "0"
                    
                    val color = if (data.isCompleted) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(text = "$displayVal\n$unit", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.Center, lineHeight = 9.sp)
                } else {
                    if (data.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = activeColor, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
            }
            
            // Bottom: Penalty
            if (data.isMissed && data.penalty > 0f) {
                Text(text = "-${data.penalty}", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}
