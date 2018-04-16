package com.example.sbazh.tensorflow.utils

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

object Labels {

    fun loadLabels(assetManager: AssetManager, labelsFileName: String) : ArrayList<String>{
        val labels = ArrayList<String>()
        BufferedReader(InputStreamReader(assetManager.open(labelsFileName))).useLines { lines ->
            lines.forEach { labels.add(it) }
        }

        return labels
    }

}