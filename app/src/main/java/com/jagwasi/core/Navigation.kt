package com.jagwasi.core

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Order : Screen("order")
    data object Payment : Screen("payment")
    data object Stock : Screen("stock")
    data object Auth : Screen("auth")
    data object Guest : Screen("guest")
    data object Governance : Screen("governance")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { /* Home screen */ }
        composable(Screen.Order.route) { /* Order screen */ }
        composable(Screen.Payment.route) { /* Payment screen */ }
        composable(Screen.Stock.route) { /* Stock screen */ }
        composable(Screen.Auth.route) { /* Auth screen */ }
        composable(Screen.Guest.route) { /* Guest screen */ }
        composable(Screen.Governance.route) { /* Governance screen */ }
    }
}
