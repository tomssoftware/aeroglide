package com.alpsfly.aeroglide.core.data.util


import com.alpsfly.aeroglide.core.model.database.Location
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class Csv(private val locations: List<Location>) {
    fun buildCsv(): String {
        val stringBuffer = StringBuffer()

        val csvFormat = CSVFormat.Builder.create().setHeader(
            "timestamp",
            "altitude",
            "bearing",
            "geoidCorrection",
            "horizontalAccuracy",
            "latitude",
            "longitude",
            "provider",
            "speed",
            "speedAccuracy",
            "verticalAccuracy"
        ).get()

        val csvPrinter = CSVPrinter(stringBuffer, csvFormat)

        locations.forEach { record ->
            csvPrinter.printRecord(
                record.timestamp,
                record.altitude,
                record.bearing,
                record.geoidCorrection,
                record.horizontalAccuracy,
                record.latitude,
                record.longitude,
                record.provider,
                record.speed,
                record.speedAccuracy,
                record.verticalAccuracy,
            )
        }

        csvPrinter.flush()
        csvPrinter.close()

        return stringBuffer.toString()
    }
}