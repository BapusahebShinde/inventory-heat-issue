package com.itek.retail.nativelib

data class DecodeResult(
    val status: Int,
    val barcode: String="",
    val serialNumber: String="",
    val message: String=""
){
    fun toMap(): HashMap<String, Any>{
        val map = HashMap<String, Any>()
        map.put("status",status)
        map.put("ean",barcode)
        map.put("serial",serialNumber)
        map.put("msg",message)
        return map;
    }
}

