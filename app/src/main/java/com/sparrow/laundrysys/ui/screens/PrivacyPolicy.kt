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
fun PrivacyPolicy(navController: NavController) {
    val policies = listOf(
        "Information We Collect" to "We collect personal information such as name, contact details, order history, and payment details necessary for providing laundry services.",
        "How We Use Your Data" to "The information collected is used to process orders, manage staff, track expenses, and improve app functionality.",
        "Data Security" to "Mizzenmast Technologies implements security measures such as encryption and access controls to protect user data. However, complete security cannot be guaranteed.",
        "Third-Party Sharing" to "We do not sell your personal data. However, we may share necessary data with payment processors, cloud storage services, or regulatory authorities when required by law.",
        "Compliance with Kenya’s Data Protection Act" to "We adhere to Kenya’s Data Protection Act, ensuring lawful, fair, and transparent processing of user data.",
        "User Rights" to "You have the right to access, correct, delete, or restrict processing of your personal data by contacting The Laundry Room’s management.",
        "Cookies & Tracking Technologies" to "The app may use cookies or analytics tools to improve user experience and track app performance.",
        "Data Retention" to "Personal data is retained for as long as necessary to fulfill service requirements and comply with legal obligations. Data no longer required will be securely deleted.",
        "Data Breach Notification" to "In case of a data breach that affects user privacy, we will notify affected users and relevant authorities in compliance with applicable laws.",
        "Explicit User Consent" to "By using the app, you consent to the collection and processing of your data as outlined in this policy.",
        "Data Minimization" to "We only collect and store the minimum data necessary for app functionality and business operations.",
        "Third-Party Integrations" to "The app may integrate with third-party services (e.g., payment providers). These services have their own privacy policies, and users are encouraged to review them.",
        "Children’s Privacy" to "The Laundry Room app is not intended for users under 13. We do not knowingly collect data from children.",
        "Changes to This Policy" to "We may update this Privacy Policy periodically. Users will be notified of significant changes via app notifications or email.",
        "Contact Information" to "If you have any questions about this policy, please contact The Laundry Room or Mizzenmast Technologies."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
            items(policies) { (title, content) ->
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
