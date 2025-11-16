package com.alpsfly.aeroglide.core.model.mapsforge

data class Region(
    val name: String,
    val url: String,
    val relativePath: String,
    val boundingBox: BoundingBox,
    val fileSizeMB: Int
)
