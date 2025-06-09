package com.dirman.dapisapp

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class LogicDeteksi(context: Context) {
    private val interpreter: Interpreter
    private val labels = listOf("Daun Sehat", "Sigatoka", "Cordana", " Pestaliopsis")
    private val imageSize = 160


    init {
        val assetFileDescriptor = context.assets.openFd("Banana_model.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val model = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(model)
    }

    fun classify(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val input = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        input.order(ByteOrder.nativeOrder())

        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixel = resizedBitmap.getPixel(x, y)
                input.putFloat(((pixel shr 16 and 0xFF) / 160.0f))
                input.putFloat(((pixel shr 8 and 0xFF) / 160.0f))
                input.putFloat(((pixel and 0xFF) / 160.0f))
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return if (maxIdx != -1) "${labels[maxIdx]} (Akurasi: ${"%.2f".format(output[0][maxIdx] * 100)}%)" else "Tidak dikenali"
    }
}