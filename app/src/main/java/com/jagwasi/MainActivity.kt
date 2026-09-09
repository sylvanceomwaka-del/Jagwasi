package com.jagwasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jagwasi.presentation.auth.AuthScreen
import com.jagwasi.presentation.auth.AuthViewModel
import com.jagwasi.presentation.governance.MasterScreen
import com.jagwasi.presentation.governance.MasterViewModel
import com.jagwasi.presentation.governance.SupervisorScreen
import com.jagwasi.presentation.governance.SupervisorViewModel
import com.jagwasi.presentation.guest.GuestScreen
import com.jagwasi.presentation.guest.GuestViewModel
import com.jagwasi.presentation.order.OrderScreen
import com.jagwasi.presentation.order.OrderViewModel
import com.jagwasi.presentation.payment.PaymentScreen
import com.jagwasi.presentation.payment.PaymentViewModel
import com.jagwasi.presentation.stock.StockScreen
import com.jagwasi.presentation.stock.StockViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JagwasiApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Order : Screen("order")
    object Payment : Screen("payment/{orderId}") {
        fun passOrderId(orderId: Int) = "payment/$orderId"
    }
    object Stock : Screen("stock")
    object Guest : Screen("guest")
    object Master : Screen("master")
    object Supervisor : Screen("supervisor")
}

@Composable
fun JagwasiApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = viewModel,
                onAuthenticated = { 
                    navController.navigate(Screen.Order.route) { 
                        popUpTo(Screen.Auth.route) { inclusive = true } 
                    } 
                },
                onBiometricRequested = { /* Handle biometric prompt */ }
            )
        }

        composable(Screen.Order.route) {
            val viewModel: OrderViewModel = hiltViewModel()
            OrderScreen(
                viewModel = viewModel,
                onNavigateToPayment = { orderId ->
                    navController.navigate(Screen.Payment.passOrderId(orderId))
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val viewModel: PaymentViewModel = hiltViewModel()
            viewModel.initialize(orderId)
            PaymentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReceipt = { _ ->
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Stock.route) {
            val viewModel: StockViewModel = hiltViewModel()
            StockScreen(
                viewModel = viewModel,
                onNavigateToRestock = { /* Placeholder – open dialog or new screen */ },
                onNavigateToWasteLog = { /* Placeholder */ }
            )
        }

        composable(Screen.Guest.route) {
            val viewModel: GuestViewModel = hiltViewModel()
            GuestScreen(
                viewModel = viewModel,
                onOrderPlaced = { _ ->
                    navController.navigate(Screen.Order.route) {
                        popUpTo(Screen.Guest.route) { inclusive = true }
                    }
                },
                onConsentRequired = { /* Show consent dialog */ }
            )
        }

        composable(Screen.Master.route) {
            val viewModel: MasterViewModel = hiltViewModel()
            MasterScreen(
                viewModel = viewModel,
                onNavigateToSupervisor = { navController.navigate(Screen.Supervisor.route) },
                onNavigateToStaffRegistration = { /* Placeholder – could open dialog or a new screen */ }
            )
        }

        composable(Screen.Supervisor.route) {
            val viewModel: SupervisorViewModel = hiltViewModel()
            SupervisorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

