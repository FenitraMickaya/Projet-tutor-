package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.utils.QrCodeGenerator

@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    qrColor: Color = MaterialTheme.colorScheme.onBackground,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val matrix = remember(content) {
        QrCodeGenerator.generateQrMatrix(content, size = 25)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rows = matrix.size
            val cols = matrix[0].size
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows

            // Clear background
            drawRect(color = backgroundColor, size = size)

            // Draw matrix squares
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(c * cellWidth, r * cellHeight),
                            size = Size(cellWidth + 0.5f, cellHeight + 0.5f) // overlapping fraction to avoid lines
                        )
                    }
                }
            }
        }
    }
}
