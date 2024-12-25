package com.alpsfly.aeroglide.core.model.common.mapbox

data class Waypoints(
    var distance: Double? = null,
    var name: String? = null,
    var location: ArrayList<Double> = arrayListOf()
)