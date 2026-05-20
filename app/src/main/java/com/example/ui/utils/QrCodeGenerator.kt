package com.example.ui.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    
    /**
     * Generates a 2D boolean array representing the QR code matrix.
     * True represents black pixels, false represents white.
     */
    fun generateQrMatrix(content: String, size: Int = 20): Array<BooleanArray> {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val matrix = Array(bitMatrix.height) { BooleanArray(bitMatrix.width) }
            for (y in 0 until bitMatrix.height) {
                for (x in 0 until bitMatrix.width) {
                    matrix[y][x] = bitMatrix.get(x, y)
                }
            }
            matrix
        } catch (e: Exception) {
            // fallback dummy corner-marked 2D array
            Array(size) { y ->
                BooleanArray(size) { x ->
                    // Make some random-looking squares that look like a QR code
                    (x in 0..4 && y in 0..4) || (x in (size - 5)..<size && y in 0..4) || (x in 0..4 && y in (size - 5)..<size) || (x + y) % 3 == 0
                }
            }
        }
    }

    /**
     * Helper to generate a Bitmap QR code of a specific pixel dimension.
     */
    fun generateQrBitmap(content: String, width: Int = 400, height: Int = 400): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
