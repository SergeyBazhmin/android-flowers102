package com.example.sbazh.tensorflow.classifier.tensorflow

import android.content.res.AssetManager
import com.example.sbazh.tensorflow.classifier.COLOR_CHANNELS
import com.example.sbazh.tensorflow.classifier.Classifier
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

object ImageClassifierFactory{

    fun create(
            assetManager: AssetManager,
            graphFilePath: String,
            imageSize: Int,
            labels: List<String>,
            inputName: String,
            outputName: String
            ): Classifier{

        return TensorFlowImageClassifier(
                inputName,
                outputName,
                imageSize.toLong(),
                labels,
                IntArray(imageSize*imageSize),
                FloatArray(imageSize*imageSize* COLOR_CHANNELS),
                FloatArray(labels.size),
                TensorFlowInferenceInterface(assetManager, graphFilePath)
        )
    }
}