package com.raktaseva.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raktaseva.app.ui.screens.auth.SplashScreen
import com.raktaseva.app.ui.screens.auth.WelcomeScreen
import com.raktaseva.app.ui.screens.auth.LoginScreen
import com.raktaseva.app.ui.screens.auth.OtpScreen
import com.raktaseva.app.ui.screens.auth.RegistrationScreen
import com.raktaseva.app.ui.screens.main.MainScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Otp : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/$phone"
    }
    object Registration : Screen("registration")
    object Main : Screen("main")
    object RequestBlood : Screen("request_blood")
    object AiMessage : Screen("ai_message")
    object AiChatbot : Screen("ai_chatbot")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNext = {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Registration.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onOtpSent = { phone ->
                    navController.navigate(Screen.Otp.createRoute(phone))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Otp.route) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpScreen(
                phone = phone,
                onVerified = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToRequest = { navController.navigate(Screen.RequestBlood.route) },
                onNavigateToChat = { navController.navigate(Screen.AiChatbot.route) }
            )
        }
        composable(Screen.RequestBlood.route) {
            com.raktaseva.app.ui.screens.requests.RequestBloodScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AiMessage.route) {
            com.raktaseva.app.ui.screens.ai.AiMessageGeneratorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AiChatbot.route) {
            com.raktaseva.app.ui.screens.ai.AiChatbotScreen(onBack = { navController.popBackStack() })
        }
    }
}
