package com.alpsfly.aeroglide.core.data.model.network.mapbox

data class Geometry(
    var coordinates: ArrayList<ArrayList<Double>> = arrayListOf(),
    var type: String? = null
)