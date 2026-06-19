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
    val isAvailable = mutableStateOf(false)
    val donationHistory = mutableStateListOf<DonationRecord>()

    fun tryParseDate(dateStr: String): Date? {
        val formats = listOf("dd/MM/yyyy", "ddMMyyyy")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault()).apply {
                    isLenient = false
                }
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }

    fun getDaysSinceLastDonation(): Long? {
        val dateStr = lastDonationDate.value.trim()
        if (dateStr.isBlank()) return null
        
        val parsedDate = tryParseDate(dateStr) ?: return null
        
        // If date is in the future, treat it as ineligible (return 0 days since donation)
        if (parsedDate.after(Date())) {
            return 0L
        }
        
        val diffInMillies = Date().time - parsedDate.time
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
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
        isAvailable.value = false
        donationHistory.clear()
        notificationsEnabled.value = true
        locationEnabled.value = true
        darkThemeEnabled.value = false
        themeMode.value = "system"
    }
}
