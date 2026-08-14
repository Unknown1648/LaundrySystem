package com.sparrow.laundrysys.utilities

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sparrow.laundrysys.classes.AuthViewModel
import com.sparrow.laundrysys.ui.components.DrawerContent
import com.sparrow.laundrysys.ui.screens.Dashboard
import com.sparrow.laundrysys.ui.screens.Expenses
import com.sparrow.laundrysys.ui.screens.ForgotPassword
import com.sparrow.laundrysys.ui.screens.Login
import com.sparrow.laundrysys.ui.screens.AdminRegistrationScreen
import com.sparrow.laundrysys.ui.screens.InitialScreen
import com.sparrow.laundrysys.ui.screens.Logs
import com.sparrow.laundrysys.ui.screens.Orders
import com.sparrow.laundrysys.ui.screens.PasswordChange
import com.sparrow.laundrysys.ui.screens.PriceList
import com.sparrow.laundrysys.ui.screens.PrivacyPolicy
import com.sparrow.laundrysys.ui.screens.Register
import com.sparrow.laundrysys.ui.screens.Settings
import com.sparrow.laundrysys.ui.screens.SetupQuestions
import com.sparrow.laundrysys.ui.screens.Staff
import com.sparrow.laundrysys.ui.screens.TermsOfService

@Composable
fun AuthenticatedApp(navController: NavHostController, authViewModel: AuthViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(drawerState = drawerState, navController = navController, scope = scope, authViewModel)
        }
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") { Dashboard(navController, drawerState, authViewModel) }
            composable(
                "orders?showCreateBottomSheet={showCreateBottomSheet}",
                arguments = listOf(navArgument("showCreateBottomSheet") { defaultValue = false })
            ) { backStackEntry ->
                val showCreateBottomSheet = backStackEntry.arguments?.getBoolean("showCreateBottomSheet") ?: false
                Orders(navController, drawerState, showCreateBottomSheet, authViewModel)
            }
            composable("expenses") { Expenses(navController, drawerState) }
            composable("price_list") { PriceList(navController, drawerState) }
            composable("staff") { Staff(navController, drawerState) }
            composable("logs") { Logs(navController, drawerState) }
            composable("settings") { Settings(navController, drawerState, authViewModel) }
            composable("register") { Register(authViewModel, navController) }
            composable("setup_questions") { SetupQuestions(navController, authViewModel) }
            composable("change_password") { PasswordChange(authViewModel, navController) }
            composable("terms_of_service") { TermsOfService(navController) }
            composable("privacy_policy") { PrivacyPolicy(navController) }
        }
    }
}

@Composable
fun UnauthenticatedApp(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        startDestination = "initial"
    ) {
        composable("initial") { InitialScreen(navController, authViewModel) }
        composable("login") { Login(navController, authViewModel) }
        composable("forgot_password") { ForgotPassword(navController, authViewModel) }
        composable("admin_registration") { AdminRegistrationScreen(navController, authViewModel) }
    }
}

