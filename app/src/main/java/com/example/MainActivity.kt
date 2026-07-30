package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.CreatePlanScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyPlansScreen
import com.example.ui.screens.PlanDetailsScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PlanoBiblicoTheme
import com.example.ui.viewmodel.BiblePlanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as Application
            val viewModel: BiblePlanViewModel = viewModel(
                factory = BiblePlanViewModel.Factory(application)
            )

            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            PlanoBiblicoTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BiblePlanApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BiblePlanApp(viewModel: BiblePlanViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onCreatePlanClick = {
                    navController.navigate(Screen.CreatePlan.route)
                },
                onViewPlanDetailsClick = { planId ->
                    navController.navigate(Screen.PlanDetails.createRoute(planId))
                },
                onMyPlansClick = {
                    navController.navigate(Screen.MyPlans.route)
                }
            )
        }

        composable(Screen.CreatePlan.route) {
            CreatePlanScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onPlanCreated = { newPlanId ->
                    navController.navigate(Screen.PlanDetails.createRoute(newPlanId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.PlanDetails.route,
            arguments = listOf(
                navArgument("planId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
            PlanDetailsScreen(
                planId = planId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MyPlans.route) {
            MyPlansScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onCreatePlanClick = {
                    navController.navigate(Screen.CreatePlan.route)
                },
                onPlanSelect = { planId ->
                    navController.navigate(Screen.PlanDetails.createRoute(planId))
                }
            )
        }
    }
}
