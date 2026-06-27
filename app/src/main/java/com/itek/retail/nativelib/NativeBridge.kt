package com.itek.retail.nativelib

class NativeBridge {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun getLibraryVersion(): String


    external fun encode(
        barcode: String,
        serial: String,
        algo: String
    ): EncodeResult

    external fun decode(
        epc: String
    ): DecodeResult
}
