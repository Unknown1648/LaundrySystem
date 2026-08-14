package com.sparrow.laundrysys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sparrow.laundrysys.classes.Logs
import com.sparrow.laundrysys.ui.theme.ErrorDark
import com.sparrow.laundrysys.ui.theme.SuccessDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Logs(navController: NavController, drawerState: DrawerState, modifier: Modifier = Modifier) {
    val firestore = FirebaseFirestore.getInstance()
    val logsCollection = firestore.collection("logs")
    var logs by remember { mutableStateOf(listOf<Logs>()) }
    val scope = rememberCoroutineScope()

    // Fetch Data from Firestore
    LaunchedEffect(Unit) {
        logsCollection.addSnapshotListener { snapshot, _ ->
            snapshot?.let { querySnapshot ->
                logs = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Logs::class.java)?.copy(id = doc.id)
                }.sortedByDescending { it.timestamp }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Logs", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {navController.popBackStack()}
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                }
            )
        }
    ) {insetsPadding ->
        if (logs.isEmpty()){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiNature,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Nothing here but crickets.",
                    color = Color.Gray
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insetsPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(logs) { log ->
                LogItem(log)
            }
        }
    }
}

@Composable
fun LogItem(log: Logs) {
    var isLogExpanded by remember { mutableStateOf(false) }
    val icon = when (log.actionType){
        "Create" -> Icons.Default.Add
        "Update" -> Icons.Default.Edit
        else -> Icons.Default.Delete
    }
    val iconTint = when (log.actionType){
        "Create" -> SuccessDark
        "Update" -> MaterialTheme.colorScheme.primary
        else -> ErrorDark
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { isLogExpanded = !isLogExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {  },
                enabled = false,
                modifier = Modifier
                    .align(Alignment.Top)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${log.actionType} order ${log.orderId} on ${formatTimestamp(log.timestamp)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = log.changedBy,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                AnimatedVisibility(isLogExpanded){
                    Column {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Old Values:", style = MaterialTheme.typography.titleSmall)
                        Text(
                            formatValues(log.oldValues),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("New Values:", style = MaterialTheme.typography.titleSmall)
                        Text(
                            formatValues(log.newValues),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            IconButton(
                onClick = { isLogExpanded = !isLogExpanded },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = if (isLogExpanded)
                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand log"
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return timestamp?.toDate()?.let { sdf.format(it) } ?: "Unknown"
}

fun formatValues(values: List<Map<String, Any>>?): String {
    return values?.joinToString("\n") { map ->
        map.entries.joinToString { "${it.key}: ${it.value}" }
    } ?: "No Data"
}

fun createLog(
    actionType: String,
    changedBy: String,
    orderId: String,
    oldValues: List<Map<String, Any>>? = null,
    newValues: List<Map<String, Any>>? = null,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val logsCollection = firestore.collection("logs")

    val logData = hashMapOf(
        "actionType" to actionType,
        "changedBy" to changedBy,
        "orderId" to orderId,
        "oldValues" to oldValues,
        "newValues" to newValues,
        "timestamp" to Timestamp.now()
    )
    logsCollection.add(logData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
}