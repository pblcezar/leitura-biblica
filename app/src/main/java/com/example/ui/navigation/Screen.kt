package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreatePlan : Screen("create_plan")
    object PlanDetails : Screen("plan_details/{planId}") {
        fun createRoute(planId: Long) = "plan_details/$planId"
    }
    object MyPlans : Screen("my_plans")
}
