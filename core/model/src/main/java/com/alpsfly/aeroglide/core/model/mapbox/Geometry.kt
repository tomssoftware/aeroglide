package com.alpsfly.aeroglide.core.model.common.mapbox

data class Geometry(
    var coordinates: ArrayList<ArrayList<Double>> = arrayListOf(),
    var type: String? = null
)