package com.example.loophabittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loophabittracker.presentation.add_edit_habit.AddEditHabitScreen
import com.example.loophabittracker.presentation.dashboard.DashboardScreen
import com.example.loophabittracker.presentation.statistics.StatisticsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToAddHabit = {
                                    navController.navigate("add_habit")
                                },
                                onNavigateToStatistics = { habitId ->
                                    navController.navigate("statistics/$habitId")
                                }
                            )
                        }
                        composable("add_habit") {
                            AddEditHabitScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "statistics/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable
                            StatisticsScreen(
                                habitId = habitId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
