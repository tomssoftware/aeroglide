package de.tomssoftware.aeroglide.core.model.common

data class Login(
    var loggedIn: Boolean? = false,
    var lastLogin: String = ""
)