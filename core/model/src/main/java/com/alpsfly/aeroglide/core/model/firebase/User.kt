package com.alpsfly.aeroglide.core.model.firebase

import com.google.firebase.firestore.PropertyName

/**
 * Represents the User account in Firestore.
 * Path: /users/{uid}
 */
data class User(
    // --- Identity ---
    @get:PropertyName("uid") // We usually duplicate the Document ID inside the object for easier serialization
    val uid: String = "",

    @get:PropertyName("email")
    val email: String = "",

    @get:PropertyName("display_name")
    val displayName: String? = null,

    @get:PropertyName("photo_url")
    val photoUrl: String? = null,

    // --- Lifecycle ---
    @get:PropertyName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("last_login_at")
    val lastLoginAt: Long = System.currentTimeMillis(),

    @get:PropertyName("is_email_verified")
    val isEmailVerified: Boolean = false,

    // --- App Tech ---
    @get:PropertyName("fcm_token") // Important for Push Notifications
    val fcmToken: String? = null,

    @get:PropertyName("version_code") // To track which app version the user is running (for migrations/debugging)
    val lastVersionCode: Int = 0,

    // --- Legal ---
    @get:PropertyName("terms_accepted")
    val termsAccepted: Boolean = false
)
