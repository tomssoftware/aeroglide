package com.alpsfly.aeroglide.core.mapsforge.layer

import com.alpsfly.aeroglide.core.model.configuration.ColorMapping
import com.alpsfly.aeroglide.core.model.mapsforge.MapLocation
import org.mapsforge.core.graphics.Canvas
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.graphics.Paint
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.model.Rotation
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.layer.Layer
import org.mapsforge.map.layer.overlay.Polyline

/**
 * Created by Thomas on 17.02.2018.
 */
class FlightPathLayer : Layer() {

    private var colorMapping = ColorMapping()
    private val polylines = mutableListOf<Polyline>()

    override fun onDestroy() {
        polylines.forEach {
            it.onDestroy()
        }
    }

    public override fun onAdd() {
        polylines.forEach {
            it.displayModel = displayModel
        }
    }

    public override fun onRemove() {
        polylines.forEach {
            it.displayModel = null
        }
    }

    @Synchronized
    override fun draw(
        boundingBox: BoundingBox?,
        zoomLevel: Byte,
        canvas: Canvas?,
        topLeftPoint: Point?,
        rotation: Rotation?
    ) {
        polylines.forEach { polyline ->
            if (polyline.displayModel != null) {
                polyline.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
            }
        }
    }

    fun addPolyLine(start: MapLocation, end: MapLocation) {
        synchronized(this) {
            val climbrate = (start.climbrate + end.climbrate) / 2f
            val paint = getPaint(getColor(climbrate), 10, Style.STROKE)
            val polyline = Polyline(paint, graphicFactory).apply {
                latLongs.add(LatLong(start.latitude, start.longitude))
                latLongs.add(LatLong(end.latitude, end.longitude))
            }
            polyline.displayModel = displayModel
            polylines.add(polyline)
        }
    }

    fun requestRedrawFlightPath() {
        requestRedraw()
    }

    private fun getColor(climbrate: Float): Int {
        return 1 // colorMapping.getClimbrateColor(climbrate) // todo: to hue
    }

    companion object {
        private val graphicFactory: GraphicFactory = AndroidGraphicFactory.INSTANCE
        private fun getPaint(color: Int, strokeWidth: Int, style: Style): Paint {
            val paint = graphicFactory.createPaint()
            paint.color = color
            paint.strokeWidth = strokeWidth.toFloat()
            paint.setStyle(style)
            paint.setDashPathEffect(floatArrayOf(25f, 15f))
            return paint
        }
    }
}
