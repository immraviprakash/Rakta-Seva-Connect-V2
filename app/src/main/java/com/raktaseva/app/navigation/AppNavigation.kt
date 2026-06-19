package com.raktaseva.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.screens.auth.SplashScreen
import com.raktaseva.app.ui.screens.auth.WelcomeScreen
import com.raktaseva.app.ui.screens.auth.LoginScreen
import com.raktaseva.app.ui.screens.auth.RegistrationScreen
import com.raktaseva.app.ui.screens.main.MainScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Main : Screen("main")
    object RequestBlood : Screen("request_blood")
    object AiMessage : Screen("ai_message")
    object AiChatbot : Screen("ai_chatbot")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
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
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val uid = currentUser.uid
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            LocalUserState.uid.value = uid
                            LocalUserState.email.value = document.getString("email") ?: currentUser.email ?: ""
                            LocalUserState.phone.value = document.getString("mobileNumber") ?: ""
                            LocalUserState.name.value = document.getString("fullName") ?: currentUser.displayName?.takeIf { it.isNotBlank() } ?: (currentUser.email?.substringBefore("@") ?: "User")
                            LocalUserState.age.value = document.getString("age") ?: ""
                            LocalUserState.gender.value = document.getString("gender") ?: ""
                            LocalUserState.bloodGroup.value = document.getString("bloodGroup") ?: "O+"
                            LocalUserState.isAvailable.value = document.getBoolean("isAvailable") ?: false
                            
                            val lastDate = document.getString("lastDonationDate") ?: ""
                            LocalUserState.lastDonationDate.value = lastDate
                            
                            if (lastDate.isNotBlank() && LocalUserState.donationHistory.none { it.date == lastDate }) {
                                LocalUserState.donationHistory.add(0, com.raktaseva.app.ui.state.DonationRecord(lastDate, "Past Record", "Completed"))
                            }
                            
                            LocalUserState.isLoggedIn.value = true
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            LocalUserState.isLoggedIn.value = true
                            LocalUserState.uid.value = uid
                            LocalUserState.email.value = currentUser.email ?: ""
                            LocalUserState.name.value = currentUser.displayName?.takeIf { it.isNotBlank() } ?: (currentUser.email?.substringBefore("@") ?: "User")
                            
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                } else {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
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
                onLoginSuccess = {
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
                onNavigateToChat = { navController.navigate(Screen.AiChatbot.route) },
                onLogoutClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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
        composable(Screen.EditProfile.route) {
            com.raktaseva.app.ui.screens.profile.EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            com.raktaseva.app.ui.screens.profile.SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
