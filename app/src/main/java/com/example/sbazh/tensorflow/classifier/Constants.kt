package com.example.sbazh.tensorflow.classifier


const val GRAPH_FILE_NAME = "optimized_inceptionV3.pb"
const val LABELS_FILE_NAME = "labels.txt"

const val GRAPH_INPUT_NODE = "input_1"
const val GRAPH_OUTPUT_NODE = "dense_1/Softmax"


const val IMAGE_SIZE = 224
const val COLOR_CHANNELS = 3
const val TRESHHOLD = 0.6
