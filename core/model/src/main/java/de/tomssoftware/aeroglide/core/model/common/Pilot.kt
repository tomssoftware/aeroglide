package de.tomssoftware.aeroglide.core.model.common

data class Pilot(
    var email: String = "",
    var displayName: String = "",
    var gliderType: String = "",
    var gliderId: String = "",
    var licence: String = "",
    var club: String = ""
)