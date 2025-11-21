package com.alpsfly.aeroglide.core.mapsforge.layer

import com.alpsfly.aeroglide.core.model.mapsforge.ThermalSpot
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


class ThermalLayer : Layer() {
    private val fillCatA = getPaint(GRAPHIC_FACTORY.createColor(60, 255, 0, 0), 5, Style.FILL)
    private val strokeCatA = getPaint(GRAPHIC_FACTORY.createColor(160, 255, 0, 0), 5, Style.STROKE)
    private val fillCatB = getPaint(GRAPHIC_FACTORY.createColor(60, 0, 0, 255), 5, Style.FILL)
    private val strokeCatB = getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 5, Style.STROKE)
    private val fillCatC = getPaint(GRAPHIC_FACTORY.createColor(60, 0, 255, 0), 5, Style.FILL)
    private val strokeCatC = getPaint(GRAPHIC_FACTORY.createColor(160, 0, 255, 0), 5, Style.STROKE)

    private val thermalCatA = Circle(LatLong(0.0, 0.0), 100f, fillCatA, strokeCatA)
    private val thermalCatB = Circle(LatLong(0.0, 0.0), 100f, fillCatB, strokeCatB)
    private val thermalCatC = Circle(LatLong(0.0, 0.0), 100f, fillCatC, strokeCatC)

    private var thermals = listOf<ThermalSpot>()

    override fun onDestroy() {
        thermalCatA.onDestroy()
        thermalCatB.onDestroy()
        thermalCatC.onDestroy()
    }

    public override fun onAdd() {
        thermalCatA.displayModel = displayModel
        thermalCatB.displayModel = displayModel
        thermalCatC.displayModel = displayModel
    }

    public override fun onRemove() {
        thermalCatA.displayModel = null
        thermalCatB.displayModel = null
        thermalCatC.displayModel = null
    }

    @Synchronized
    override fun draw(
        boundingBox: BoundingBox?,
        zoomLevel: Byte,
        canvas: Canvas?,
        topLeftPoint: Point?,
        rotation: Rotation?
    ) {
        drawThermals(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
    }


    @Synchronized
    private fun drawThermals(
        boundingBox: BoundingBox?,
        zoomLevel: Byte,
        canvas: Canvas?,
        topLeftPoint: Point?,
        rotation: Rotation?
    ) {
        val thermals = this.thermals.toList()
        thermals.forEach { thermal ->
            when (thermal.category) {
                'A' -> {
                    thermalCatA.setLatLong(LatLong(thermal.latitude, thermal.longitude))
                    thermalCatA.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
                }

                'B' -> {
                    thermalCatB.setLatLong(LatLong(thermal.latitude, thermal.longitude))
                    thermalCatB.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
                }

                'C' -> {
                    thermalCatC.setLatLong(LatLong(thermal.latitude, thermal.longitude))
                    thermalCatC.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation)
                }
            }
        }
    }

    @Synchronized
    fun updateThermals(thermals: List<ThermalSpot>) {
        this.thermals = thermals
        requestRedraw()
    }

    companion object {
        private val GRAPHIC_FACTORY: GraphicFactory = AndroidGraphicFactory.INSTANCE

        private fun getPaint(color: Int, strokeWidth: Int, style: Style): Paint {
            val paint = GRAPHIC_FACTORY.createPaint()
            paint.color = color
            paint.strokeWidth = strokeWidth.toFloat()
            paint.setStyle(style)
            return paint
        }
    }
}