package com.alpsfly.aeroglide.core.model.firebase

import com.alpsfly.aeroglide.core.model.common.Auth
import com.alpsfly.aeroglide.core.model.common.Disclaimer
import com.alpsfly.aeroglide.core.model.common.Login

data class User(
    var timestamp: Long = 0,
    var timezone: String = "",
    var user: Auth = Auth(),
    var disclaimer: Disclaimer = Disclaimer(),
    var login: Login = Login()
)
