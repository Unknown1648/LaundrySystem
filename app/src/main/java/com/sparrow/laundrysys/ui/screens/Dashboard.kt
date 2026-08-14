package com.sparrow.laundrysys.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sparrow.laundrysys.classes.AuthViewModel
import com.sparrow.laundrysys.classes.Expense
import com.sparrow.laundrysys.classes.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    navController: NavController,
    drawerState: DrawerState,
    auth: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val ordersState = remember { mutableStateOf<List<Order>>(emptyList()) }
    val expensesState = remember { mutableStateOf<List<Expense>>(emptyList()) }
    val metricsState = remember { mutableStateOf(DashboardMetrics(0, 0.0, 0.0)) }
    val weeklyOrdersData = remember { mutableStateOf<Pair<List<Int>, List<String>>>(Pair(emptyList(), emptyList())) }
    val isLoading = remember { mutableStateOf(true) }
    val user = auth.currentUser
    // Derive pending orders from main order list
    val pendingOrders = ordersState.value.filter { it.status != "Completed" }

    // Fetch orders and calculate metrics
    LaunchedEffect(Unit) {
        db.collection("orders").addSnapshotListener { orderSnapshot, orderError ->
            if (orderError != null) {
                Log.e("Dashboard", "Error fetching orders", orderError)
            }

            db.collection("expenses").addSnapshotListener { expenseSnapshot, expenseError ->
                if (expenseError != null) {
                    Log.e("Dashboard", "Error fetching expenses", expenseError)
                }


                val orders = orderSnapshot?.documents?.mapNotNull { documentToOrder(it) } ?: emptyList()
                val expenses = expenseSnapshot?.documents?.mapNotNull { documentToExpense(it) } ?: emptyList()

                ordersState.value = orders
                expensesState.value = expenses

                // Calculate metrics and update state
                metricsState.value = calculateMetrics(orders, expenses)
                weeklyOrdersData.value = calculateWeeklyOrdersData(orders)

                isLoading.value = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                }
            )
        }
    ) { insetsPadding ->
        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DashboardContent(
                username = user.value!!.username,
                metrics = metricsState.value,
                pendingOrders = pendingOrders,
                weeklyOrdersData = weeklyOrdersData.value,
                navController = navController,
                db = db,
                modifier = modifier.padding(insetsPadding)
            )
        }
    }
}

data class DashboardMetrics(
    val todayOrderCount: Int,
    val todayRevenue: Double,
    val todayExpenses: Double
)

@Composable
private fun DashboardContent(
    username: String,
    metrics: DashboardMetrics,
    pendingOrders: List<Order>,
    weeklyOrdersData: Pair<List<Int>, List<String>>,
    navController: NavController,
    db: FirebaseFirestore,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Today's overview section
        item {
            DashboardSection(title = "Today's overview") {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricsCard(Icons.Default.Payments,"Revenue", String.format(Locale.getDefault(), "%.1f", metrics.todayRevenue))
                    MetricsCard(Icons.Default.ShoppingBasket,"Orders", "${metrics.todayOrderCount}")
                    MetricsCard(Icons.Default.Wallet,"Expenses", String.format(Locale.getDefault(), "%.1f", metrics.todayExpenses))
                }
            }
        }

        // Quick Actions section
        item {
            DashboardSection(title = "Quick Actions") {
                QuickActionsGrid(navController = navController)
            }
        }

        // Orders History section
        item {
            DashboardSection(title = "Orders History") {
                OrdersBarChart(
                    data = weeklyOrdersData.first,
                    labels = weeklyOrdersData.second
                )
            }
        }

        // Pending orders empty state
        item {
            DashboardSection(title = "Orders in progress") {
                if (pendingOrders.isEmpty()) {
                    EmptyStateMessage(message = "No orders in progress")
                }
            }
        }
        // Pending orders list
        items(pendingOrders) { order ->
            OrderCard(
                username = username,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                customerLocation = order.customerLocation,
                totalPrice = order.totalPrice,
                status = order.status,
                orderId = order.id,
                currentStatus = order.status,
                db = db
            )
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    content()
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun QuickActionsGrid(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickAction(
                label = "Create Order",
                onClick = { navController.navigate("orders?showCreateBottomSheet=true") },
                icon = Icons.Default.AddCircle
            )
            QuickAction(
                label = "Log Expense",
                onClick = { navController.navigate("expenses") },
                icon = Icons.Default.DateRange
            )
            QuickAction(
                label = "Edit Prices",
                onClick = { navController.navigate("price_list") },
                icon = Icons.Default.Edit
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickAction(
                label = "View Orders",
                onClick = { navController.navigate("orders?showCreateBottomSheet=false") },
                icon = Icons.Default.ShoppingCart
            )
            QuickAction(
                label = "View Logs",
                onClick = { navController.navigate("logs") },
                icon = Icons.Default.Info
            )
            QuickAction(
                label = "Manage Staff",
                onClick = { navController.navigate("staff") },
                icon = Icons.Default.Person
            )
        }
    }
}

// Empty state message
@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper function to calculate weekly orders data
private fun calculateWeeklyOrdersData(orders: List<Order>): Pair<List<Int>, List<String>> {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val currentYear = calendar.get(Calendar.YEAR)

    // Create day labels in reverse order (most recent day is last)
    val dayLabels = List(7) { dayIndex ->
        calendar.set(Calendar.DAY_OF_YEAR, today - dayIndex)
        calendar.set(Calendar.YEAR, currentYear)
        SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase()
    }.reversed()

    // Count orders for each day in the last week
    val orderCounts = List(7) { dayIndex ->
        val dayStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.DAY_OF_YEAR, today - 6 + dayIndex)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val dayEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.DAY_OF_YEAR, today - 6 + dayIndex)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        // Count orders created on this day
        orders.count { order ->
            val orderDate = order.dateCreated?.toDate()
            orderDate != null && orderDate >= dayStart && orderDate <= dayEnd
        }
    }

    return Pair(orderCounts, dayLabels)
}

// Helper function to calculate dashboard metrics
private fun calculateMetrics(orders: List<Order>, expenses: List<Expense>): DashboardMetrics {
    // Get today's date at midnight
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.time

    // Filter today's orders
    val todayOrders = orders.filter { order ->
        order.dateCreated?.toDate()?.let { it >= startOfDay } ?: false
    }

    // Today's expenses
    val todayExpenses = expenses.filter { expense ->
        expense.date?.toDate()?.let { it >= startOfDay } ?: false
    }

    return DashboardMetrics(
        todayOrderCount = todayOrders.size,
        todayRevenue = todayOrders.sumOf { it.totalPrice },
        todayExpenses = todayExpenses.sumOf { it.amount }
    )
}

// Helper function to convert DocumentSnapshot to Order object
private fun documentToOrder(doc: DocumentSnapshot): Order {
    return Order(
        id = doc.id,
        customerPhone = doc.getString("customerPhone") ?: "",
        customerName = doc.getString("customerName") ?: "",
        customerLocation = doc.getString("customerLocation") ?: "",
        status = doc.getString("status") ?: "",
        paid = doc.getBoolean("paid") ?: false,
        totalPrice = doc.getDouble("totalPrice") ?: 0.0,
        serviceItems = doc.get("serviceItems") as? List<Map<String, Double>>,
        dateCreated = doc.getTimestamp("dateCreated")
    )
}

// Helper function to convert DocumentSnapshot to Expense object
private fun documentToExpense(doc: DocumentSnapshot): Expense {
    return Expense(
        id = doc.id,
        title = doc.getString("title") ?: "",
        category = doc.getString("category") ?: "",
        notes = doc.getString("notes") ?: "",
        amount = doc.getDouble("amount") ?: 0.0,
        date = doc.getTimestamp("date")
    )
}

// Updated OrderCard with logging functionality
@Composable
private fun OrderCard(
    username: String,
    customerName: String,
    customerPhone: String,
    customerLocation: String,
    totalPrice: Double,
    status: String,
    orderId: String,
    currentStatus: String,
    db: FirebaseFirestore
) {
    val context = LocalContext.current
    val nextStatus = when(status) {
        "Pending" -> "Cleaning"
        "Cleaning" -> "Drying"
        "Drying" -> "Delivery"
        else -> "Completed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = customerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
            Text(text = "📞   $customerPhone", style = MaterialTheme.typography.bodyMedium)
            Text(text = "🗺️   $customerLocation", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "💰   ${String.format(Locale.getDefault(),"%.2f",totalPrice)} Kshs",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer, ShapeDefaults.Medium)
                ){
                    Text(
                        status,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Button(
                    onClick = {
                        // Store the old status before updating
                        val oldStatus = status

                        // Update the order status
                        updateOrderStatus(db, orderId, currentStatus)

                        // Create a log of this status change
                        createLog(
                            actionType = "Update",
                            changedBy = username,
                            orderId = orderId,
                            oldValues = listOf(mapOf("status" to oldStatus)),
                            newValues = listOf(mapOf("status" to nextStatus)),
                            onSuccess = {
                                // Show success toast
                                Toast.makeText(context, "Order updated to $nextStatus", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                // Show error toast
                                Toast.makeText(context, "Failed to log update: ${exception.message}", Toast.LENGTH_SHORT).show()
                                Log.e("OrderCard", "Failed to log status change", exception)
                            }
                        )
                    },
                    shape = ShapeDefaults.Small
                ) {
                    Text("Next: $nextStatus", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}


// OrdersBarChart implementation
@Composable
fun OrdersBarChart(data: List<Int>, labels: List<String>) {
    if (data.isEmpty() || labels.isEmpty()) {
        EmptyStateMessage(message = "No order data available")
        return
    }

    val maxValue = data.maxOrNull() ?: 0
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Orders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Chart content
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            data.forEachIndexed { index, value ->
                if (index < labels.size) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Display the actual count above each bar
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor
                        )

                        // Bar with animation
                        val barHeight = remember { Animatable(0f) }
                        LaunchedEffect(value) {
                            barHeight.animateTo(
                                targetValue = if (maxValue > 0) value.toFloat() / maxValue else 0f,
                                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(barHeight.value)
                                .background(
                                    barColor,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        // X-axis label
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAction(label: String, onClick: () -> Unit, icon: ImageVector) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
            }
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun MetricsCard(imageVector: ImageVector, title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(120.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.Center) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(imageVector, null)
                Spacer(Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}