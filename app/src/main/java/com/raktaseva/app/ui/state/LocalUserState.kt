package com.raktaseva.app.ui.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DonationRecord(
    val date: String,
    val hospital: String,
    val status: String
)

object LocalUserState {
    val isLoggedIn = mutableStateOf(false)
    val uid = mutableStateOf("")
    val email = mutableStateOf("")
    val phone = mutableStateOf("")
    val name = mutableStateOf("Guest")
    val age = mutableStateOf("")
    val gender = mutableStateOf("")
    val bloodGroup = mutableStateOf("O+")
    val lastDonationDate = mutableStateOf("")
    val intent = mutableStateOf("")
    val donationHistory = mutableStateListOf<DonationRecord>()

    fun getDaysSinceLastDonation(): Long? {
        if (lastDonationDate.value.isBlank()) return null
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val lastDate = sdf.parse(lastDonationDate.value) ?: return null
            val diffInMillies = Math.abs(Date().time - lastDate.time)
            TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            null
        }
    }

    fun isEligibleToDonate(): Boolean {
        val days = getDaysSinceLastDonation() ?: return true
        return days >= 90
    }

    fun getDaysUntilEligible(): Long {
        val days = getDaysSinceLastDonation() ?: return 0
        val remaining = 90 - days
        return if (remaining > 0) remaining else 0
    }
    
    // Settings
    val notificationsEnabled = mutableStateOf(true)
    val locationEnabled = mutableStateOf(true)
    val darkThemeEnabled = mutableStateOf(false)
    val themeMode = mutableStateOf("system") // "system", "light", "dark"
    
    fun clear() {
        isLoggedIn.value = false
        uid.value = ""
        email.value = ""
        phone.value = ""
        name.value = "Guest"
        age.value = ""
        gender.value = ""
        bloodGroup.value = "O+"
        lastDonationDate.value = ""
        intent.value = ""
        donationHistory.clear()
        notificationsEnabled.value = true
        locationEnabled.value = true
        darkThemeEnabled.value = false
        themeMode.value = "system"
    }
}
