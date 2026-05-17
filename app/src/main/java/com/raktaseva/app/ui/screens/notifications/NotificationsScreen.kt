package com.raktaseva.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.data.model.Notification
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.theme.Dimens
import androidx.compose.foundation.layout.offset
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    var notifications by remember { mutableStateOf(emptyList<Notification>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("notifications")
            .whereEqualTo("targetUserUid", LocalUserState.uid.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(Dimens.screenHorizontal), contentAlignment = Alignment.Center) {
                    Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                    contentPadding = PaddingValues(top = Dimens.spacingSm, bottom = Dimens.screenVertical)
                ) {
                    items(notifications, key = { it.notificationId }) { notif ->
                        NotificationCard(notif)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(notification: Notification) {
    val db = FirebaseFirestore.getInstance()

    val cardColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }

    Card(
        onClick = {
            if (!notification.isRead) {
                db.collection("notifications").document(notification.notificationId).update("isRead", true)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(Dimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator dot
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = 6.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                ) {}
                Spacer(modifier = Modifier.width(Dimens.spacingMd))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    val dateStr = try {
                        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        sdf.format(notification.createdAt.toDate())
                    } catch (e: Exception) {
                        "Just now"
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
