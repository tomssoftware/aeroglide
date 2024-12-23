package com.alpsfly.aeroglide.core.data.model.network.firebase

import com.alpsfly.aeroglide.core.data.model.network.Disclaimer
import com.alpsfly.aeroglide.core.data.model.network.Login
import com.alpsfly.aeroglide.core.data.model.network.Auth

data class User (
    var timestamp: Long = 0,
    var timezone: String = "",
    var user: Auth = Auth(),
    var disclaimer: Disclaimer = Disclaimer(),
    var login: Login = Login()
)
