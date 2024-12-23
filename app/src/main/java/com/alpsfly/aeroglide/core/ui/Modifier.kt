package com.alpsfly.aeroglide.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate

class CenteredModifier : DrawModifier {
    override fun ContentDrawScope.draw() {
        val center = Offset(size.width / 2, size.height / 2)
        translate(center.x, center.y) {
            this@draw.drawContent()
        }
    }
}

fun Modifier.centered() = this.then(CenteredModifier())