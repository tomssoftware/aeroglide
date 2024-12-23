package com.alpsfly.aeroglide.core.data.model.network.mapbox

data class Legs(
    var steps: ArrayList<String> = arrayListOf(),
    var summary: String? = null,
    var weight: Double? = null,
    var duration: Double? = null,
    var distance: Double? = null
)