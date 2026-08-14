package com.sparrow.laundrysys.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfService(navController: NavController) {
    val terms = listOf(
        "Agreement to Terms" to "By using The Laundry Room app, you agree to these Terms of Service, forming a binding agreement between you and Mizzenmast Technologies.",
        "Use of the App" to "The Laundry Room app is designed for managing orders, expenses, and staff. Unauthorized use, including tampering with system functionality, is prohibited.",
        "Account Registration" to "Users must provide accurate details during registration. You are responsible for maintaining the confidentiality of your account.",
        "Service Availability" to "We strive for uninterrupted service but do not guarantee uptime. The app may experience outages due to maintenance or unforeseen circumstances.",
        "User Responsibilities" to "Users agree to comply with all applicable laws, respect privacy, and avoid misuse of data accessed through the app.",
        "Payment and Transactions" to "All payments processed through the app must be legitimate. We are not liable for third-party payment processor failures.",
        "Intellectual Property" to "All content within the app, including trademarks and software, belongs to Mizzenmast Technologies or The Laundry Room. Unauthorized reproduction is prohibited.",
        "Limitation of Liability" to "Mizzenmast Technologies is not responsible for any loss or damage resulting from use of the app, including data breaches or operational issues.",
        "Privacy and Data Protection" to "We adhere to Kenya’s Data Protection Act and ensure fair handling of personal data as outlined in our Privacy Policy.",
        "Modifications to Terms" to "We reserve the right to modify these terms at any time. Continued use after updates constitutes acceptance of the revised terms.",
        "Termination of Access" to "We may suspend or terminate user access if they violate these terms or engage in harmful activities within the app.",
        "Governing Law" to "These terms are governed by the laws of Kenya. Disputes arising from the app’s use will be resolved under Kenyan jurisdiction.",
        "Contact Information" to "For inquiries about these terms, contact The Laundry Room or Mizzenmast Technologies."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(terms) { (title, content) ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
