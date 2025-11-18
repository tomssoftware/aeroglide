package com.alpsfly.aeroglide.core.mapsforge.layer

import android.content.Context
import com.alpsfly.aeroglide.core.mapsforge.R
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
import org.mapsforge.map.layer.overlay.Circle
import org.mapsforge.map.layer.overlay.Marker


/**
 * Created by Thomas on 17.02.2018.
 */

class LocationMarkerLayer : Layer() {
    private var circle: Circle? = null
    private var mapNeedle: RotatingMarker? = null
    private var mapNeedlePinned: Marker? = null
    private var mapNeedleOff: Marker? = null
    private var marker: Marker? = null
    private var showAccuracy = true

    public override fun onAdd() {
        circle?.displayModel = displayModel
        mapNeedle?.displayModel = displayModel
        mapNeedlePinned?.displayModel = displayModel
        mapNeedleOff?.displayModel = displayModel
    }

    public override fun onRemove() {
        circle?.displayModel = null
        mapNeedle?.displayModel = null
        mapNeedlePinned?.displayModel = null
        mapNeedleOff?.displayModel = null
    }

    override fun onDestroy() {
        mapNeedle?.onDestroy()
        mapNeedlePinned?.onDestroy()
        mapNeedleOff?.onDestroy()
    }

    @Synchronized
    override fun draw(
        boundingBox: BoundingBox?,
        zoomLevel: Byte,
        canvas: Canvas?,
        topLeftPoint: Point?,
        rotation: Rotation?
    ) {
        if (showAccuracy) {
            circle?.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
        }
        marker?.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
    }


    fun setup(context: Context?) {
        val circleFill = getPaint(graphicFactory.createColor(48, 0, 0, 255), 0, Style.FILL)
        val circleStroke = getPaint(graphicFactory.createColor(160, 0, 0, 255), 2, Style.STROKE)
        circle = Circle(null, 0f, circleFill, circleStroke)

        mapNeedlePinned = Marker(
            null, AndroidGraphicFactory.convertToBitmap(
                context?.resources?.getDrawable(R.drawable.ic_map_needle_pinned, null)
            ), 0, 0
        )
        mapNeedle = RotatingMarker(
            null, AndroidGraphicFactory.convertToBitmap(
                context?.resources?.getDrawable(R.drawable.ic_map_needle, null)
            ), 0, 0
        )
        mapNeedleOff = Marker(
            null, AndroidGraphicFactory.convertToBitmap(
                context?.resources?.getDrawable(R.drawable.ic_map_needle_off, null)
            ), 0, 0
        )
        marker = mapNeedleOff
    }

    fun updateNeedle(latLong: LatLong, accuracy: Float) {
        marker?.latLong = latLong
        circle?.setLatLong(latLong)
        if (accuracy == 0f) {
            marker = mapNeedleOff
        }
        circle?.radius = accuracy

        requestRedraw()
    }

    fun updateMarker(speed: Float, bearing: Float) {
        if (speed < 1) {
            marker = mapNeedlePinned
        } else {
            mapNeedle?.setDegree(bearing)
            marker = mapNeedle
        }

        requestRedraw()
    }

    companion object {
        private val TAG = LocationMarkerLayer::class.java.simpleName

        protected val graphicFactory: GraphicFactory = AndroidGraphicFactory.INSTANCE
        protected fun getPaint(color: Int, strokeWidth: Int, style: Style): Paint {
            val paint = graphicFactory.createPaint()
            paint.color = color
            paint.strokeWidth = strokeWidth.toFloat()
            paint.setStyle(style)
            return paint
        }
    }
}
