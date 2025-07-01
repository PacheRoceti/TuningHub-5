package com.example.tuninghub


data class UploadResponse(
    val success: Boolean,
    val message: String,
    val logs: List<String>? = null
)

