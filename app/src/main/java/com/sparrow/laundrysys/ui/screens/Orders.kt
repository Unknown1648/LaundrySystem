package com.sparrow.laundrysys.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sparrow.laundrysys.classes.AuthViewModel
import com.sparrow.laundrysys.classes.PriceItem
import com.sparrow.laundrysys.utilities.generateInvoice
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Orders(
    navController: NavController,
    drawerState: DrawerState,
    showCreateBottomSheet: Boolean,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var isBottomSheetVisible by remember { mutableStateOf(showCreateBottomSheet) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchWidth by animateDpAsState(if (isSearchExpanded) 260.dp else 0.dp, label = "SearchWidth")
    var searchString by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val username = authViewModel.currentUser.value?.username
    val db = FirebaseFirestore.getInstance()
    val orders = remember { mutableStateListOf<DocumentSnapshot>() }

    // Fetch orders from Firestore
    LaunchedEffect(Unit) {
        db.collection("orders").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                orders.clear()
                orders.addAll(it.documents)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchExpanded) {
                        Text("Orders", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(
                            visible = isSearchExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            OutlinedTextField(
                                value = searchString,
                                onValueChange = { searchString = it },
                                placeholder = { Text("Search orders...") },
                                singleLine = true,
                                modifier = Modifier
                                    .width(searchWidth)
                                    .padding(horizontal = 8.dp),
                                shape = ShapeDefaults.ExtraLarge
                            )
                        }

                        IconButton(onClick = { searchString = ""; isSearchExpanded = !isSearchExpanded }) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Toggle search"
                            )
                        }

                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isBottomSheetVisible = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "New order")
            }
        }
    ) { insetsPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insetsPadding)
                .consumeWindowInsets(insetsPadding)
                .padding(12.dp)
        ) {
            // Status Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                val statuses = listOf("All", "Pending", "Cleaning", "Drying", "Delivery", "Completed")
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Filtered Orders List
            val filteredOrders = orders.filter {
                (selectedStatus == "All" || it.getString("status") == selectedStatus) &&
                        (searchString.isBlank() || it.getString("customerName")?.contains(searchString, ignoreCase = true) == true)
            }

            if(filteredOrders.isEmpty()){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No orders found",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the + button to add a new order",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn {
                items(filteredOrders) { document ->
                    val orderId = document.id
                    val customerName = document.getString("customerName") ?: "Unknown"
                    val status = document.getString("status") ?: "Pending"
                    val totalPrice = document.getDouble("totalPrice") ?: 0.0
                    val serviceItems = document["serviceItems"] as? List<Map<String, Any>> ?: emptyList()
                    var orderExpanded by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    val statusFlow = listOf("Pending", "Cleaning", "Drying", "Delivery", "Completed")
                    val nextStatus = statusFlow.getOrNull(statusFlow.indexOf(status) + 1) ?: "Completed"

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        onClick = { orderExpanded = !orderExpanded},
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Order Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Order for $customerName", style = MaterialTheme.typography.titleMedium)
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
                            }

                            Spacer(Modifier.height(8.dp))

                            // Animated Expandable Service Items
                            AnimatedVisibility(visible = orderExpanded) {
                                Column {
                                    serviceItems.forEach { item ->
                                        val itemName = item["item"] as? String ?: "Unknown Item"
                                        val quantity = item["quantity"] as? Double ?: 0.0
                                        val total = item["total"] as? Double ?: 0.0
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier.fillMaxWidth(.9f)
                                        ) {
                                            Text(itemName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                                            Text("Qty: $quantity", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            Text("Kshs $total", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Total Price
                            Text("Total: Kshs $totalPrice", fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (orderExpanded)
                                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand"
                                )
                                Row {
                                    // Edit Button
                                    IconButton(onClick = { selectedOrderId = orderId }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Order")
                                    }

                                    // Status Update Button (only if not completed)
                                    if (status != "Completed") {
                                        Button(
                                            onClick = {
                                                updateOrderStatus(db, orderId, status)
                                                createLog(
                                                    actionType = "Update",
                                                    changedBy = username!!,
                                                    orderId = orderId,
                                                    oldValues = listOf(mapOf("status" to status)),
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
                                            Text("Next Status")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reusable Dialog for New Order
    if (isBottomSheetVisible) {
        OrderDialog(
            isNew = true,
            username = username!!,
            onDismiss = { isBottomSheetVisible = false },
            onSave = { orderData ->
                db.collection("orders")
                    .add(orderData)
                    .addOnSuccessListener {
                        isBottomSheetVisible = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding order", e)
                    }
            }
        )
    }

    // Reusable Dialog for Edit Order
    selectedOrderId?.let { orderId ->
        val orderDoc = orders.find { it.id == orderId }
        orderDoc?.let { document ->
            val customerName = document.getString("customerName") ?: "Unknown"
            val phone = document.getString("customerPhone") ?: ""
            val location = document.getString("customerLocation") ?: ""
            val status = document.getString("status") ?: "Pending"
            val totalPrice = document.getDouble("totalPrice") ?: 0.0
            val serviceItems = document["serviceItems"] as? List<Map<String, Any>> ?: emptyList()
            val isPaid = document.getBoolean("isPaid") ?: false

            OrderDialog(
                isNew = false,
                username = username!!,
                initialOrderId = orderId,
                initialCustomerName = customerName,
                initialPhone = phone,
                initialLocation = location,
                initialTotalPrice = totalPrice,
                initialServiceItems = serviceItems,
                initialIsPaid = isPaid,
                initialStatus = status,
                onDismiss = { selectedOrderId = null },
                onSave = { updatedOrder ->
                    updateOrderDetails(db, orderId, updatedOrder)
                    selectedOrderId = null
                },
                onDelete = { orderIdToDelete ->
                    db.collection("orders").document(orderIdToDelete)
                        .delete()
                        .addOnSuccessListener {
                            selectedOrderId = null
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error deleting order", e)
                        }
                }
            )
        }
    }
}

// Function to move order to next status
fun updateOrderStatus(db: FirebaseFirestore, orderId: String, currentStatus: String) {
    val statusFlow = listOf("Pending", "Cleaning", "Drying", "Delivery", "Completed")
    val nextStatus = statusFlow.getOrNull(statusFlow.indexOf(currentStatus) + 1) ?: "Completed"

    db.collection("orders").document(orderId)
        .update("status", nextStatus)
        .addOnSuccessListener { Log.d("Firestore", "Order status updated to $nextStatus") }
        .addOnFailureListener { Log.e("Firestore", "Failed to update status", it) }
}

fun updateOrderDetails(db: FirebaseFirestore, orderId: String, updatedOrder: Map<String, Any>) {
    db.collection("orders").document(orderId)
        .update(updatedOrder)
        .addOnSuccessListener {
            Log.d("Firestore", "Order updated successfully")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error updating order", e)
        }
}

// Validate order before submit
fun validateOrder(
    customerName: String,
    customerPhone: String,
    location: String,
    selectedItems: SnapshotStateList<Map<PriceItem, Double>>,
    discount: String
): String? {
    if (customerName.isBlank()) return "Customer name is required."
    if (customerPhone.isBlank()) return "Phone number is required."
    if (!customerPhone.matches(Regex("\\d{9,}"))) return "Enter a valid phone number (9+ digits)."
    if (location.isBlank()) return "Location is required."
    if (selectedItems.isEmpty()) return "Select at least one service item."
    if (discount.isNotBlank() && discount.toDoubleOrNull() == null) return "Invalid discount amount."

    return null  // No errors
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDialog(
    isNew: Boolean,
    username: String,
    initialOrderId: String = "",
    initialCustomerName: String = "",
    initialPhone: String = "",
    initialLocation: String = "",
    initialTotalPrice: Double = 0.0,
    initialServiceItems: List<Map<String, Any>> = emptyList(),
    initialIsPaid: Boolean = false,
    initialStatus: String = "Pending",
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var customerName by remember { mutableStateOf(initialCustomerName) }
    var phone by remember { mutableStateOf(initialPhone.replace("+254", "")) }
    var location by remember { mutableStateOf(initialLocation) }
    var totalPrice by remember { mutableDoubleStateOf(initialTotalPrice) }
    var isPaid by remember { mutableStateOf(initialIsPaid) }
    var discount by remember { mutableStateOf("") }
    val status by remember { mutableStateOf(initialStatus) }
    var showPriceListDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Transform initial service items to the format used by the dialog
    val transformedInitialItems = initialServiceItems.map { item ->
        val priceItem = PriceItem(
            item = item["item"] as String,
            price =  ((item["price"] as? Double) ?: 0.0),
            uom =  (item["uom"] as? String) ?: "pcs"
        )
        val quantity = (item["quantity"] as? Double) ?: 1.0
        mapOf(priceItem to quantity)
    }

    val selectedItems = remember { mutableStateListOf<Map<PriceItem, Double>>() }
    
    val serviceItemsList = selectedItems.map {
        val item = it.keys.first()
        val quantity = it.values.first()
        mapOf(
            "item" to item.item,
            "price" to item.price,
            "quantity" to quantity,
            "uom" to item.uom,
            "total" to item.price * quantity
        )
    }

    // Initialize selectedItems with transformed data
    LaunchedEffect(initialServiceItems) {
        selectedItems.clear()
        selectedItems.addAll(transformedInitialItems)
    }

    fun removeItem(index: Int) {
        val removedItem = selectedItems[index]
        totalPrice -= removedItem.keys.first().price * removedItem.values.first()
        selectedItems.removeAt(index)
    }

    fun saveOrder() {
        val order = hashMapOf(
            "customerName" to customerName,
            "customerPhone" to "+254$phone",
            "customerLocation" to location,
            "serviceItems" to serviceItemsList,
            "totalPrice" to totalPrice - (discount.toDoubleOrNull() ?: 0.0),
            "isPaid" to isPaid,
            "status" to status
        )

        // Add creation timestamp for new orders
        if (isNew) {
            order["dateCreated"] = FieldValue.serverTimestamp()
        }

        onSave(order)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxSize(),
        dragHandle = { }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isNew) "Create Order" else "Edit Order") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = { showPriceListDialog = true }) {
                            Icon(Icons.Default.Add, "Add")
                            Text("Add Item")
                        }
                    }
                )
            }
        ) { insets ->
            Column(modifier = Modifier.padding(insets).padding(12.dp)) {

                // Customer Details Inputs
                Column {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Customer phone") },
                        prefix = { Text("+254") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Customer location") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text("Services", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))

                // Selected Items List
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item { HorizontalDivider() }

                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("#", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Item Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Price", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Qty (UoM)", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Actions", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    item { HorizontalDivider() }

                    itemsIndexed(selectedItems) { index, item ->
                        val priceItem = item.keys.first()
                        val quantity = item.values.first()
                        val itemTotal = priceItem.price * quantity

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall)
                            Text(priceItem.item, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
                            Text("${priceItem.price}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text("$quantity (${priceItem.uom})", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall)
                            Text("$itemTotal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { removeItem(index) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Order Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total: Kshs $totalPrice", style = MaterialTheme.typography.titleMedium)

                        // Only show discount field for new orders
                        if (isNew) {
                            OutlinedTextField(
                                value = discount,
                                onValueChange = { discount = it },
                                label = { Text("Discount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                singleLine = true
                            )

                            Text(
                                "Total Payable: Kshs ${totalPrice - (discount.toDoubleOrNull() ?: 0.0)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Show status for existing orders
                        if (!isNew) {
                            Text("Status: $status", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Payment Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isPaid, onClick = { isPaid = true })
                            Text("Paid")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isPaid, onClick = { isPaid = false })
                            Text("Unpaid")
                        }
                    }
                }

                errorMessage?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                }

                // Confirmation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isNew && onDelete != null) {
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancel")
                        }
                    }

                    Button(
                        onClick = {
                            // For new orders, validate first
                            if (isNew) {
                                errorMessage = validateOrder(customerName, phone, location, selectedItems, discount)
                                if (errorMessage == null) {
                                    val orderId = UUID.randomUUID().toString()
                                    saveOrder()

                                    // Generate pdf invoice
                                    generateInvoice(
                                        context = context,
                                        orderId = orderId,
                                        customerName = customerName,
                                        customerPhone = "+254$phone",
                                        customerLocation = location,
                                        serviceItems = serviceItemsList,
                                        totalPrice = totalPrice - (discount.toDoubleOrNull() ?: 0.0),
                                        isPaid = isPaid
                                    )

                                    createLog(
                                        actionType = "Create",
                                        changedBy = username,
                                        orderId = orderId,
                                        oldValues = null,
                                        newValues = listOf(
                                            mapOf(
                                                "customerName" to customerName,
                                                "customerPhone" to "+254$phone",
                                                "customerLocation" to location,
                                                "serviceItems" to serviceItemsList,
                                                "totalPrice" to (totalPrice - (discount.toDoubleOrNull() ?: 0.0)),
                                                "isPaid" to isPaid,
                                                "status" to status
                                            )
                                        ),
                                        onSuccess = {
                                            Toast.makeText(context, "Order created successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { exception ->
                                            Toast.makeText(context, "Failed to log order creation: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            Log.e("OrderDialog", "Failed to log order creation", exception)
                                        }
                                    )
                                }
                            } else {
                                saveOrder()
                                // Generate pdf invoice
                                generateInvoice(
                                    context = context,
                                    orderId = initialOrderId,
                                    customerName = customerName,
                                    customerPhone = "+254$phone",
                                    customerLocation = location,
                                    serviceItems = serviceItemsList,
                                    totalPrice = totalPrice,
                                    isPaid = isPaid
                                )
                                createLog(
                                    actionType = "Update",
                                    changedBy = username,
                                    orderId = initialOrderId,
                                    oldValues = listOf(
                                        mapOf(
                                            "customerName" to initialCustomerName,
                                            "customerPhone" to initialPhone,
                                            "customerLocation" to initialLocation,
                                            "serviceItems" to initialServiceItems,
                                            "totalPrice" to initialTotalPrice,
                                            "isPaid" to initialIsPaid,
                                            "status" to initialStatus
                                        )
                                    ),
                                    newValues = listOf(
                                        mapOf(
                                            "customerName" to customerName,
                                            "customerPhone" to "+254$phone",
                                            "customerLocation" to location,
                                            "serviceItems" to serviceItemsList,
                                            "totalPrice" to totalPrice,
                                            "isPaid" to isPaid,
                                            "status" to status
                                        )
                                    ),
                                    onSuccess = {
                                        Toast.makeText(context, "Order updated successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { exception ->
                                        Toast.makeText(context, "Failed to log update: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("OrderDialog", "Failed to log update", exception)
                                    }
                                )
                            }
                        }
                    ) {
                        Text(if (isNew) "CheckOut" else "Save")
                    }
                }
            }

            // Delete confirmation dialog
            if (showDeleteConfirmation && onDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    confirmButton = {
                        Button(onClick = {
                            onDelete(initialOrderId)
                            createLog(
                                actionType = "Delete",
                                changedBy = username,
                                orderId = initialOrderId,
                                oldValues = listOf(
                                    mapOf(
                                        "customerName" to initialCustomerName,
                                        "customerPhone" to initialPhone,
                                        "customerLocation" to initialLocation,
                                        "serviceItems" to initialServiceItems,
                                        "totalPrice" to initialTotalPrice,
                                        "isPaid" to initialIsPaid,
                                        "status" to initialStatus
                                    )
                                ),
                                newValues = null,
                                onSuccess = {
                                    Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(context, "Failed to log delete: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("OrderDialog", "Failed to log delete", exception)
                                }
                            )
                            showDeleteConfirmation = false
                        }) {
                            Text("Confirm Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this order?") }
                )
            }

            // Price list dialog
            if (showPriceListDialog) {
                PriceListDialog(
                    onItemSelected = { item, quantity ->
                        selectedItems.add(mapOf(item to quantity))
                        totalPrice += (item.price * quantity)
                        showPriceListDialog = false
                    },
                    onDismiss = { showPriceListDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListDialog(onItemSelected: (PriceItem, Double) -> Unit, onDismiss: () -> Unit) {
    var selectedQuantity by remember { mutableStateOf("") }
    val quantityAsDouble = selectedQuantity.toDoubleOrNull() ?: 1.0
    var selectedItem by remember { mutableStateOf<PriceItem?>(null) }
    val db = FirebaseFirestore.getInstance()
    var priceList by remember { mutableStateOf(listOf<PriceItem>()) }

    // Fetch price list
    LaunchedEffect(Unit) {
        db.collection("pricelist").get().addOnSuccessListener { result ->
            priceList = result.documents.mapNotNull { it.toObject(PriceItem::class.java) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Select an Item", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f) // Ensures text field remains visible
                ) {
                    items(priceList) { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedItem == item)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedItem = item }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = item.item)
                                if (selectedItem == item) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "Selected"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = selectedQuantity,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            selectedQuantity = it
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            selectedItem?.let { onItemSelected(it, quantityAsDouble) }
                            onDismiss()
                        },
                        enabled = selectedItem != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    )
}