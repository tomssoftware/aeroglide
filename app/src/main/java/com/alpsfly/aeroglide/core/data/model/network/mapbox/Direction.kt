package com.alpsfly.aeroglide.core.data.model.network.mapbox

data class Direction(
    var routes: ArrayList<Routes> = arrayListOf(),
    var waypoints: ArrayList<Waypoints> = arrayListOf(),
    var code: String? = null,
    var uuid: String? = null
)