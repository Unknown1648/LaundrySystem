package com.sparrow.laundrysys.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sparrow.laundrysys.R
import com.sparrow.laundrysys.classes.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    navController: NavController,
    scope: CoroutineScope,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navItems = listOf(
        Triple(Icons.Default.Store, "Dashboard", "dashboard"),
        Triple(Icons.Default.ShoppingBasket, "Orders", "orders"),
        Triple(Icons.Default.Wallet, "Expenses", "expenses"),
        Triple(Icons.Default.Paid, "Price List", "price_list"),
        Triple(Icons.Default.People, "Staff", "staff"),
        Triple(Icons.Default.Summarize, "Logs", "logs"),
        Triple(Icons.Default.Settings, "Settings", "settings")
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?.substringBefore("?")

    ModalDrawerSheet(
        modifier = modifier,
        drawerState = drawerState
    ) {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Logo
                Image(
                    painterResource(R.drawable.full_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // navItems
                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.first, contentDescription = null) },
                        label = { Text(item.second, style = MaterialTheme.typography.bodyMedium) },
                        selected = item.third == currentRoute,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.third)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    },
                    selected = false,
                    label = { Text("Sign Out") },
                    onClick = {
                        authViewModel.logout(context)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}