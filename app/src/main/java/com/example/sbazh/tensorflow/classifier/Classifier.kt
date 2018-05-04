package com.example.sbazh.tensorflow.classifier

import android.graphics.Bitmap

interface Classifier {
    fun recognizeImage(bitmap:Bitmap): Recognition
}