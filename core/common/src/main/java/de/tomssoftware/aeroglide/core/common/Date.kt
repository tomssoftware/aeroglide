package de.tomssoftware.aeroglide.core.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun toLocalDateString(timestamp: Long): String {
    val dd = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val strDate = dd.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)).toString()
    return strDate
}
