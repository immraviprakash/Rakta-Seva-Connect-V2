package com.raktaseva.app.data.model

import com.google.firebase.Timestamp

data class BloodRequest(
    val requestId: String = "",
    val requesterUid: String = "",
    val requesterName: String = "",
    val requesterEmail: String = "",
    val bloodGroup: String = "",
    val hospitalName: String = "",
    val hospitalLocation: String = "",
    val unitsRequired: Int = 0,
    val urgencyLevel: String = "",
    val additionalNotes: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val requestStatus: String = "active",
    val donorResponsesCount: Int = 0,
    val responders: List<String> = emptyList()
)
