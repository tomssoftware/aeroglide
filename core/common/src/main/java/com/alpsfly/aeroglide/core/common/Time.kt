package com.alpsfly.aeroglide.core.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun toLocalTimeString(timestamp: Long): String {
    val dd = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val strTime = dd.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)).toString()
    return strTime
}

fun toLocalDurationString(duration: Long): String {
    return String.format("%02d:%02d", duration / 3600, (duration % 3600) / 60) // todo: fix
}


