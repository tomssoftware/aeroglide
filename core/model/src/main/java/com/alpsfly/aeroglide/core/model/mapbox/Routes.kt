package com.alpsfly.aeroglide.core.model.common.mapbox



data class Routes(

    var geometry: Geometry? = Geometry(),
    var legs: ArrayList<Legs> = arrayListOf(),
    var weightName: String? = null,
    var weight: Double? = null,
    var duration: Double? = null,
    var distance: Double? = null

)