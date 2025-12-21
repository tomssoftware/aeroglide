package com.alpsfly.aeroglide.core.model.firebase

data class Blacklist(
    var versions: ArrayList<Int> = arrayListOf(),
    var users: ArrayList<String> = arrayListOf()
)