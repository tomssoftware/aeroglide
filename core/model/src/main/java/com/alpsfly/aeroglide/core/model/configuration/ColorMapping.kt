package com.alpsfly.aeroglide.core.model.configuration

class ColorMapping {

    private var colorsAscent = mutableListOf(
        "#96ED89", // 0.0 .. 0.2
        "#A5F286", // 0.2 .. 0.4
        "#B4F785", // 0.4 .. 0.6
        "#C3FC84", // 0.6 .. 0.8
        "#D2FF83", // 0.8 .. 1.0
        "#E1FF82", // 1.0 .. 1.2
        "#FFA012", // 1.2 .. 1.4
        "#FFB114", // 1.4 .. 1.6
        "#FFC412", // 1.6 .. 1.8
        "#FFD412", // 1.8 .. 2.0
        "#FFE814", // 2.0 .. 2.2
        "#EF5411", // 2.2 .. 2.4
        "#FA5B0F", // 2.4 .. 2.6
        "#FF6517", // 2.6 .. 2.8
        "#FF6D1F", // 2.8 .. 3.0
        "#FF822E", // 3.0 ..
        "#450003"
    )

    private var colorsDescent = mutableListOf(
        "#96ED89", // 0.0 .. 0.2
        "#82E4DF", // 0.2 .. 0.4
        "#6ED9D5", // 0.4 .. 0.6
        "#5ACECB", // 0.6 .. 0.8
        "#46C3C1", // 0.8 .. 1.0
        "#32B8B7", // 1.0 .. 1.2
        "#1EA7AD", // 1.2 .. 1.4
        "#0EEAFF", // 1.4 .. 1.6
        "#15A9FA", // 1.6 .. 1.8
        "#1B76FF", // 1.8 .. 2.0
        "#1C3FFD", // 2.0 .. 2.2
        "#1441F7", // 2.2 .. 2.4
        "#1510F0", // 2.4 .. 2.6
        "#0003C7", // 2.6 .. 2.8
        "#0E1E8C", // 2.8 .. 3.0
        "#010340"  // 3.0 ..
    )

    private fun getColorAscent(climbrate: Float): String {
        return when ((climbrate * 5f).toInt()) {
            in 0 .. 1 -> colorsAscent[0]
            in 1 .. 2 -> colorsAscent[1]
            in 2 .. 3 -> colorsAscent[2]
            in 3 .. 4 -> colorsAscent[3]
            in 4 .. 5 -> colorsAscent[4]
            in 5 .. 6 -> colorsAscent[5]
            in 6 .. 7 -> colorsAscent[6]
            in 7 .. 8 -> colorsAscent[7]
            in 8 .. 9 -> colorsAscent[8]
            in 9 .. 10 -> colorsAscent[9]
            in 10 .. 11 -> colorsAscent[10]
            in 11 .. 12 -> colorsAscent[11]
            in 12 .. 13 -> colorsAscent[12]
            in 13 .. 14 -> colorsAscent[13]
            in 14 .. 15 -> colorsAscent[14]
            in 15 .. 16 -> colorsAscent[15]
            else -> colorsAscent[16]
        }
    }

    private fun getColorDescent(climbrate: Float): String {
        return when ((climbrate * -5f).toInt()) {
            in 0 .. 1 -> colorsDescent[0]
            in 1 .. 2 -> colorsDescent[1]
            in 2 .. 3 -> colorsDescent[2]
            in 3 .. 4 -> colorsDescent[3]
            in 4 .. 5 -> colorsDescent[4]
            in 5 .. 6 -> colorsDescent[5]
            in 6 .. 7 -> colorsDescent[6]
            in 7 .. 8 -> colorsDescent[7]
            in 8 .. 9 -> colorsDescent[8]
            in 9 .. 10 -> colorsDescent[9]
            in 10 .. 11 -> colorsDescent[10]
            in 11 .. 12 -> colorsDescent[11]
            in 12 .. 13 -> colorsDescent[12]
            in 13 .. 14 -> colorsDescent[13]
            in 14 .. 15 -> colorsDescent[14]
            else -> colorsDescent[15]
        }
    }

    fun getClimbrateColor(climbrate: Float): String {
        return if (climbrate > 0) {
            getColorAscent(climbrate)
        } else {
            getColorDescent(climbrate)
        }
    }
}