#include <jni.h>
#include <string.h>  // for memset
#include "include/main.h"

// =========================com.itek.retail.NativeLib
// Utility: Create EncodeResult object  com/itek/retail/NativeLib
// =========================
jobject createEncodeResult(JNIEnv *env, int status, const char *epc, const char *message) {
    jclass cls = (*env)->FindClass(env, "com/itek/retail/nativelib/EncodeResult");
    if (!cls) return NULL;  // Defensive check

    jmethodID ctor = (*env)->GetMethodID(env, cls, "<init>", "(ILjava/lang/String;Ljava/lang/String;)V");
    if (!ctor) return NULL;

    jstring jEpc = epc ? (*env)->NewStringUTF(env, epc) : "";
    jstring jMessage = message ? (*env)->NewStringUTF(env, message) : "";

    return (*env)->NewObject(env, cls, ctor, status, jEpc, jMessage);
}

// =========================
// Utility: Create DecodeResult object
// =========================
jobject createDecodeResult(JNIEnv *env, int status, const char *barcode, const char *serialNumber, const char *message) {
    jclass cls = (*env)->FindClass(env, "com/itek/retail/nativelib/DecodeResult");
    if (!cls) return NULL;

    jmethodID ctor = (*env)->GetMethodID(env, cls, "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (!ctor) return NULL;

    jstring jBarcode = barcode ? (*env)->NewStringUTF(env, barcode) : "";
    jstring jSerialNumber = serialNumber ? (*env)->NewStringUTF(env, serialNumber) : "";
    jstring jMessage = message ? (*env)->NewStringUTF(env, message) : "";

    return (*env)->NewObject(env, cls, ctor, status, jBarcode, jSerialNumber, jMessage);
}

// =========================
// JNI: Get Library Version
// =========================
JNIEXPORT jstring JNICALL
Java_com_itek_retail_nativelib_NativeBridge_getLibraryVersion(JNIEnv *env, jobject thiz) {
    const char *version = get_itek_library_version();  // From your native library
    return (*env)->NewStringUTF(env, version);
}

// =========================
// JNI: Encode
// =========================
JNIEXPORT jobject JNICALL
Java_com_itek_retail_nativelib_NativeBridge_encode(JNIEnv *env, jobject thiz, jstring barcode_, jstring serial_, jstring algo_) {
    const char *barcode = (*env)->GetStringUTFChars(env, barcode_, 0);
    const char *serial = (*env)->GetStringUTFChars(env, serial_, 0);
    const char *algo = (*env)->GetStringUTFChars(env, algo_, 0);

    EncodeResult result;
    memset(&result, 0, sizeof(result));

    int status = itek_encode(barcode, serial, &result, algo);

    (*env)->ReleaseStringUTFChars(env, barcode_, barcode);
    (*env)->ReleaseStringUTFChars(env, serial_, serial);
    (*env)->ReleaseStringUTFChars(env, algo_, algo);

    if (status != 0 || result.status == 0) {
        return createEncodeResult(env, -1, "", "Encoding failed");
    }

    return createEncodeResult(env, 0, result.epc, "");
}

// =========================
// JNI: Decode
// =========================
JNIEXPORT jobject JNICALL
Java_com_itek_retail_nativelib_NativeBridge_decode(JNIEnv *env, jobject thiz, jstring epc_) {
    const char *epc = (*env)->GetStringUTFChars(env, epc_, 0);

    DecodeResult result;
    memset(&result, 0, sizeof(result));

    int status = itek_decode(epc, &result);

    (*env)->ReleaseStringUTFChars(env, epc_, epc);

    if (status != 0 || result.status == 0) {
        return createDecodeResult(env, -1, "", "", "Decoding failed");
    }

    return createDecodeResult(env, 0, result.barcode, result.serialNumber, "");
}
