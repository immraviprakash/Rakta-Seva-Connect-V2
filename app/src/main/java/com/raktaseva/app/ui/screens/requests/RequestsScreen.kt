package com.raktaseva.app.ui.screens.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.data.model.BloodRequest
import com.raktaseva.app.ui.screens.home.EmergencyRequestItem
import com.raktaseva.app.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var requests by remember { mutableStateOf(emptyList<BloodRequest>()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(com.raktaseva.app.ui.state.LocalUserState.bloodGroup.value) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("requests")
            .whereEqualTo("requestStatus", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    android.widget.Toast.makeText(context, "Error loading requests: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allRequests = snapshot.documents.mapNotNull { it.toObject(BloodRequest::class.java) }
                    requests = allRequests.filter { request ->
                        request.requesterUid == com.raktaseva.app.ui.state.LocalUserState.uid.value ||
                        com.raktaseva.app.utils.BloodCompatibility.isCompatibleDonor(
                            donorGroup = com.raktaseva.app.ui.state.LocalUserState.bloodGroup.value,
                            neededGroup = request.bloodGroup
                        )
                    }
                    isLoading = false
                }
            }
        onDispose {
            listener.remove()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("All Requests", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLg),
            contentPadding = PaddingValues(top = Dimens.spacingSm, bottom = Dimens.screenVertical)
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(Dimens.spacingHuge), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (requests.isEmpty()) {
                item {
                    Text(
                        "No active requests available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Dimens.spacingLg)
                    )
                }
            } else {
                items(requests, key = { it.requestId }) { request ->
                    EmergencyRequestItem(
                        request = request,
                        showSnackbar = { msg ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }
            }
        }
    }
}
