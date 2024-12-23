package com.alpsfly.aeroglide.core.data.model.network

data class Login(
    var loggedIn: Boolean? = false,
    var lastLogin: String = ""
)