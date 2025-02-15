package com.alpsfly.aeroglide.core.model.configuration

import android.graphics.Color

class ColorMapping {

    private var colorsAscent = mutableListOf(
        "#96ED89", // 0.0 .. 0.3
        // https://colordesigner.io/#FFA012-FFB114-FFC412-FFD412-FFE814
        "#FFA012", // 0.3 ..
        "#FFB114", // 0.5 ..
        "#FFC412", // 0.7 ..
        "#FFD412", // 0.9 ..
        "#FFE814", // 1.1 ..
        // https://colordesigner.io/#EF5411-FA5B0F-FF6517-FF6D1F-FF822E
        "#EF5411", // 1.3
        "#FA5B0F", // 1.5 ..
        "#FF6517", // 1.7 ..
        "#FF6D1F", // 1.9 ..
        "#FF822E", // 2.1 ..
        // https://colordesigner.io/#450003-5C0002-94090D-D40D12-FF1D23
        "#EF5411", // 2.3 ..
        "#FA5B0F", // 2.5 ..
        "#FF6517", // 2.7 ..
        "#FF6D1F", // 2.9 ..
        "#FF822E"  // 3.1 ..
    )

    private var colorsDescent = mutableListOf(
        "#96ED89", // 0.0 .. 0.5
        // https://colordesigner.io/#0EEAFF-15A9FA-1B76FF-1C3FFD-2C1DFF
        "#0EEAFF", // 0.5 ..
        "#15A9FA", // 0.8 ..
        "#1B76FF", // 1.1 ..
        "#1C3FFD", // 1.4 ..
        "#2C1DFF", // 1.7 ..
        // https://colordesigner.io/#010340-0E1E8C-0003C7-1510F0-1441F7
        "#1441F7", // 2.0 ..
        "#1510F0", // 2.3 ..
        "#0003C7", // 2.6 ..
        "#0E1E8C", // 2.9 ..
        "#010340"  // 3.0 ..
    )

    fun getColorAscent(climbrate: Float) : String {
        return when ((climbrate * 10f).toInt()) {
            in 0 .. 3 -> colorsAscent[0]
            in 3 .. 5 -> colorsAscent[1]
            in 5 .. 7 -> colorsAscent[2]
            in 7 .. 9 -> colorsAscent[3]
            in 9 .. 11 -> colorsAscent[4]
            in 11 .. 13 -> colorsAscent[5]
            in 13 .. 15 -> colorsAscent[6]
            in 15 .. 17 -> colorsAscent[7]
            in 17 .. 19 -> colorsAscent[8]
            in 19 .. 21 -> colorsAscent[9]
            in 21 .. 23 -> colorsAscent[10]
            in 23 .. 25 -> colorsAscent[11]
            in 25 .. 27 -> colorsAscent[12]
            in 27 .. 29 -> colorsAscent[13]
            in 29 .. 31 -> colorsAscent[14]
            in 31 .. 33 -> colorsAscent[15]
            else -> colorsAscent[15]
        }
    }

    fun getColorDescent(climbrate: Float) : String {
        return when ((climbrate * -10f).toInt()) {
            in 0 .. 5 -> colorsDescent[0]
            in 5 .. 8 -> colorsDescent[1]
            in 8 .. 11 -> colorsDescent[2]
            in 11 .. 14 -> colorsDescent[3]
            in 14 .. 17 -> colorsDescent[4]
            in 17 .. 21 -> colorsDescent[5]
            in 20 .. 23 -> colorsDescent[6]
            in 23 .. 26 -> colorsDescent[7]
            in 26 .. 29 -> colorsDescent[8]
            in 29 .. 31 -> colorsDescent[9]
            in 31 .. 33 -> colorsDescent[10]
            else -> colorsDescent[10]
        }
    }

    fun getClimbrateColor(climbrate: Float) : String {
        return if (climbrate > 0) {
            getColorAscent(climbrate)
        } else {
            getColorDescent(climbrate)
        }
    }

    fun getGradeColorAscent(grade: Float) : String {
        return when (grade) {
            in 0f .. 1f -> colorsAscent[0]
            in 1f .. 2f -> colorsAscent[1]
            in 2f .. 3f -> colorsAscent[2]
            in 3f .. 4f -> colorsAscent[3]
            in 4f .. 5f -> colorsAscent[4]
            in 5f .. 6f -> colorsAscent[5]
            in 6f .. 7f -> colorsAscent[6]
            in 7f .. 8f -> colorsAscent[7]
            in 8f .. 9f -> colorsAscent[8]
            in 9f .. 10f -> colorsAscent[9]
            in 10f .. 11f -> colorsAscent[10]
            in 11f .. 12f -> colorsAscent[11]
            in 12f .. 13f -> colorsAscent[12]
            in 13f .. 14f -> colorsAscent[13]
            in 14f .. 15f -> colorsAscent[14]
            in 15f .. 16f -> colorsAscent[15]
            else -> colorsAscent[15]
        }
    }

    fun getGradeColorDescent(grade: Float) : String {
        return when (grade.toInt()) {
            in -0 downTo -1 -> colorsDescent[0]
            in -1 downTo -2 -> colorsDescent[1]
            in -2 downTo -3 -> colorsDescent[2]
            in -3 downTo -4 -> colorsDescent[3]
            in -4 downTo -5 -> colorsDescent[4]
            in -5 downTo -6 -> colorsDescent[5]
            in -6 downTo -8 -> colorsDescent[6]
            in -8 downTo -10 -> colorsDescent[7]
            in -10 downTo -12 -> colorsDescent[8]
            in -12 downTo -14 -> colorsDescent[9]
            in -14 downTo -16 -> colorsDescent[10]
            else -> colorsDescent[10]
        }
    }
}