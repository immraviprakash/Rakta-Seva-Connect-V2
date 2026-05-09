package com.raktaseva.app.ui.screens.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.screens.home.EmergencyRequestItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("All Requests", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(10) {
                EmergencyRequestItem()
            }
        }
    }
}
