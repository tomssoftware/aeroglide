package com.alpsfly.aeroglide.core.mapsforge.layer

import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.graphics.Canvas
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.model.Rectangle
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.layer.overlay.Marker


/**
 * Created by Thomas on 17.02.2018.
 */

class RotatingMarker(latLong: LatLong?, bitmap: Bitmap, horizontalOffset: Int, verticalOffset: Int) :
    Marker(latLong, bitmap, horizontalOffset, verticalOffset) {

    private var degree = 0.0f
    private var px = 0.0f
    private var py = 0.0f

    @Synchronized
    fun draw(boundingBox: BoundingBox?, zoomLevel: Byte, canvas: Canvas, topLeftPoint: Point) {
        if (latLong == null || bitmap == null) {
            return
        }

        val mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.tileSize)
        val pixelX = MercatorProjection.longitudeToPixelX(latLong.longitude, mapSize)
        val pixelY = MercatorProjection.latitudeToPixelY(latLong.latitude, mapSize)
        val halfBitmapWidth = bitmap.width / 2
        val halfBitmapHeight = bitmap.height / 2
        val left = (pixelX - topLeftPoint.x - halfBitmapWidth.toDouble() + horizontalOffset).toInt()
        val top = (pixelY - topLeftPoint.y - halfBitmapHeight.toDouble() + verticalOffset).toInt()
        val right = left + bitmap.width
        val bottom = top + bitmap.height
        val bitmapRectangle = Rectangle(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())
        val canvasRectangle = Rectangle(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

        if (!canvasRectangle.intersects(bitmapRectangle)) {
            return
        }

        val androidCanvas = AndroidGraphicFactory.getCanvas(canvas)
        androidCanvas.save()
        androidCanvas.rotate(degree, (pixelX - topLeftPoint.x).toFloat(), (pixelY - topLeftPoint.y).toFloat())
        canvas.drawBitmap(bitmap, left, top)
        androidCanvas.restore()
    }

    fun setDegree(degree: Float): RotatingMarker {
        this.degree = degree
        return this
    }

    fun setPivotPoint(px: Float, py: Float): RotatingMarker {
        this.px = px
        this.py = py
        return this
    }
}
