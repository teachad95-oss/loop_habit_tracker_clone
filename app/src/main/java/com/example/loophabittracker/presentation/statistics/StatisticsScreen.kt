package com.example.loophabittracker.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    habitId: Int,
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    IconButton(onClick = { /* TODO: Edit */ }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White) }
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
                .verticalScroll(rememberScrollState())
        ) {
            // Overview Section
            SectionTitle("Overview", habitColor)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStat("Score", "${String.format("%.0f", uiState.strength)}%", habitColor)
                OverviewStat("Month", "+${String.format("%.0f", uiState.scoreMonth)}%", habitColor)
                OverviewStat("Year", "+${String.format("%.0f", uiState.scoreYear)}%", habitColor)
                OverviewStat("Total", "${uiState.totalCompletions}", habitColor)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Score Chart
            SectionTitle("Score", habitColor)
            if (uiState.recentScores.isNotEmpty()) {
                val chartEntryModel = entryModelOf(*uiState.recentScores.toTypedArray())
                Chart(
                    chart = lineChart(
                        axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0f, maxY = 100f)
                    ),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.height(200.dp).padding(16.dp)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // History Chart
            SectionTitle("History", habitColor)
            if (uiState.historyCounts.isNotEmpty()) {
                val barEntryModel = entryModelOf(*uiState.historyCounts.toTypedArray())
                Chart(
                    chart = columnChart(),
                    model = barEntryModel,
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.height(200.dp).padding(16.dp)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Calendar
            SectionTitle("Calendar", habitColor)
            CalendarHeatmap(uiState.calendarRecords, habitColor)
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun OverviewStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = valueColor, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CalendarHeatmap(records: Map<Long, Boolean>, activeColor: Color) {
    // A simplified continuous grid of 30 days
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp),
        userScrollEnabled = false
    ) {
        items(35) { i ->
            val date = LocalDate.now().minusDays((34 - i).toLong())
            val isCompleted = records[date.toEpochDay()] == true
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCompleted) activeColor else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
