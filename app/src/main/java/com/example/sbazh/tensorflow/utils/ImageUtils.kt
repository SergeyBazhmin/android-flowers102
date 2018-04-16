package com.example.sbazh.tensorflow.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.example.sbazh.tensorflow.classifier.IMAGE_SIZE

object ImageUtils{

    fun getCroppedBitmap(bitmap: Bitmap): Bitmap{
        val croppedBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val transformationMatrix = getPhotoBitmapTransformationMatrix(bitmap)
        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(bitmap, transformationMatrix, null)
        return croppedBitmap
    }


    private fun getPhotoBitmapTransformationMatrix(bitmap: Bitmap): Matrix{
        val frameToCropMatrix = getTransformationMatrix(
                bitmap.width,
                bitmap.height,
                IMAGE_SIZE,
                IMAGE_SIZE,
                0,
                true)

        val cropToFrameMatrix = Matrix()
        frameToCropMatrix.invert(cropToFrameMatrix)
        return frameToCropMatrix
    }


    private fun getTransformationMatrix(
            srcWidth: Int,
            srcHeight: Int,
            dstWidth: Int,
            dstHeight: Int,
            applyRotation: Int,
            maintainAspectRatio: Boolean) : Matrix {

        val matrix = Matrix()

        with(matrix){
            postTranslate(-srcWidth/2.0f, -srcHeight/2.0f)
            postRotate(applyRotation.toFloat())
        }

        val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        if (inWidth != dstWidth || inHeight != dstHeight)
        {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAspectRatio){
                val scaleFactor = Math.max(scaleFactorX,scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }

        matrix.postTranslate(dstWidth /2.0f, dstHeight /2.0f)

        return matrix
    }
}