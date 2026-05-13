package com.raktaseva.app.data.model

import com.google.firebase.Timestamp

data class Notification(
    val notificationId: String = "",
    val targetUserUid: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedRequestId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
