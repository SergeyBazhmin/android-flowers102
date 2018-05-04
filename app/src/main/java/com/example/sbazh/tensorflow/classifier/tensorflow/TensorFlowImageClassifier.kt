package com.example.sbazh.tensorflow.classifier.tensorflow

import android.graphics.Bitmap
import com.example.sbazh.tensorflow.classifier.*
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*

private const val ENABLE_LOGGING = true

class TensorFlowImageClassifier(
        private val inputName: String,
        private val outputName: String,
        private val imageSize: Long,
        private val labels: List<String>,
        private val imageBitmapPixels: IntArray,
        private val imageNormalizedPixels: FloatArray,
        private val results: FloatArray,
        private val tensorFlowInferenceInterface: TensorFlowInferenceInterface
) : Classifier {

    override fun recognizeImage(bitmap: Bitmap): Recognition {
        normalizeImage(bitmap)
        classifyAndFetch()
        val outputQueue = getResults()

        return outputQueue.poll()
    }

    private fun normalizeImage(bitmap: Bitmap){
        val imageMean = 117
        val imageStd = 128.0f
        bitmap.getPixels(imageBitmapPixels, 0, bitmap.width, 0,0,bitmap.width,bitmap.height)
        for (i in imageBitmapPixels.indices){
            val pixel = imageBitmapPixels[i]
            imageNormalizedPixels[i*3 + 0] = ((pixel shr 16 and 0xFF) - imageMean)/imageStd
            imageNormalizedPixels[i*3 + 1] = ((pixel shr 8 and 0xFF) - imageMean)/imageStd
            imageNormalizedPixels[i*3 + 2] = ((pixel and 0xFF) - imageMean)/imageStd
        }
    }

    private fun classifyAndFetch(){
        tensorFlowInferenceInterface.feed(inputName, imageNormalizedPixels,
                1L, imageSize, imageSize, COLOR_CHANNELS.toLong())
        tensorFlowInferenceInterface.run(arrayOf(outputName), ENABLE_LOGGING)
        tensorFlowInferenceInterface.fetch(outputName, results)
    }

    private fun getResults() : PriorityQueue<Recognition>{
        val outputQueue = PriorityQueue<Recognition>(
                labels.size,
                kotlin.Comparator { (_, rConfidence), (_, lConfidence) ->
                    java.lang.Float.compare(lConfidence,rConfidence) })
        results.indices.mapTo(outputQueue){ Recognition(labels[it], results[it]) }

        return outputQueue
    }
}