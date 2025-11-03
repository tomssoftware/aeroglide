package com.alpsfly.aeroglide.core.data.util

import com.alpsfly.aeroglide.core.model.database.Location
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class Gpx(private val locations: List<Location>) {
    @OptIn(ExperimentalTime::class)
    fun buildPath(): String {
        val xmlSerializer = XmlPullParserFactory.newInstance().newSerializer()
        return xmlSerializer.document {
            element("gpx") {
                attribute("version", "1.1")
                attribute("creator", "alpsfly.com")
                attribute("xmlns", "http://www.topografix.com/GPX/1/1")
                attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                attribute(
                    "xsi:schemaLocation",
                    "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"
                )
                element("metadata") {
                    element("name") {}
                    element("author") {
                        element("link") {
                            attribute("href", "https://alpsfly.com")
                            element("text", "alpsfly.com") {}
                            element("type", "text/html") {}
                        }
                    }
                }
                element("trk") {
                    element("trkseg") {
                        locations.forEach { it ->
                            element("trkpt") {
                                attribute("lat", it.latitude.toString())
                                attribute("lon", it.longitude.toString())
                                element("ele", it.altitude.toString()) {}
                                element("time", Instant.fromEpochMilliseconds(it.timestamp).toString()) {}
                            }
                        }
                    }
                }
            }
        }
    }
}