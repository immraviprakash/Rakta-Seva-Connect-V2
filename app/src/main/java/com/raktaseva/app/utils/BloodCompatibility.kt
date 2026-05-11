package com.raktaseva.app.utils

object BloodCompatibility {
    private val compatibilityMap = mapOf(
        "O-" to listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"),
        "O+" to listOf("O+", "A+", "B+", "AB+"),
        "A-" to listOf("A-", "A+", "AB-", "AB+"),
        "A+" to listOf("A+", "AB+"),
        "B-" to listOf("B-", "B+", "AB-", "AB+"),
        "B+" to listOf("B+", "AB+"),
        "AB-" to listOf("AB-", "AB+"),
        "AB+" to listOf("AB+")
    )

    /**
     * Returns true if [donorGroup] can donate to [recipientGroup].
     */
    fun canDonateTo(donorGroup: String, recipientGroup: String): Boolean {
        val compatibleRecipients = compatibilityMap[donorGroup.uppercase()] ?: return false
        return compatibleRecipients.contains(recipientGroup.uppercase())
    }

    /**
     * Returns true if [donorGroup] can donate to [recipientGroup].
     * Example: O- can donate to O+, A+, B+, AB+
     */
    fun isCompatibleDonor(donorGroup: String, neededGroup: String): Boolean {
        return canDonateTo(donorGroup, neededGroup)
    }
}
