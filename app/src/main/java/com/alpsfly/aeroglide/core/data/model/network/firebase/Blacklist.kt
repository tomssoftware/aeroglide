package com.alpsfly.aeroglide.core.data.model.network.firebase

data class Blacklist(
    var versions: ArrayList<Int> = arrayListOf(),
    var users: ArrayList<String> = arrayListOf()
)