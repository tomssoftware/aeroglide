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
