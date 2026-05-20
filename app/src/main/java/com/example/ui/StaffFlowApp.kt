package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AdminMainScreen
import com.example.ui.screens.EmployeeMainScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.viewmodel.StaffViewModel

@Composable
fun StaffFlowApp(
    modifier: Modifier = Modifier,
    viewModel: StaffViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToAdmin = {
                    navController.navigate("admin_main") {
                        // Pop up to welcome to clear login from stack
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToEmployee = {
                    navController.navigate("employee_main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_main") {
            AdminMainScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("admin_main") { inclusive = true }
                    }
                }
            )
        }

        composable("employee_main") {
            EmployeeMainScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("employee_main") { inclusive = true }
                    }
                }
            )
        }
    }
}
