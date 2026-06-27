package com.itek.retail.nativelib

data class EncodeResult(
    val status: Int,
    val epc: String="",
    val message: String=""
)