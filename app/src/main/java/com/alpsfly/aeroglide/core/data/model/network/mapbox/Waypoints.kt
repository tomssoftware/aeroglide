package com.alpsfly.aeroglide.core.data.model.network.mapbox

data class Waypoints(
    var distance: Double? = null,
    var name: String? = null,
    var location: ArrayList<Double> = arrayListOf()
)