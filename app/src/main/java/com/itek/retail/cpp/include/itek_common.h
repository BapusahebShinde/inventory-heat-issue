#ifndef ITEK_COMMON_H
#define ITEK_COMMON_H

// #ifdef __cplusplus
// extern "C" {
// #endif

#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>


/*
 * i-Tek Encode-Decode Library Version
 * 26.01.07.1
 */
#define Lib_Version         "26.01.08.1"


/* ============================================================================
 * CONSTANTS
 * ============================================================================
 */
/////////////////////////////////////// itek_sgtin96 //////////////////////////////////////
#define SGTIN96_HEADER          0x30
#define SGTIN96_EPC_CHARS       24
#define SGTIN96_BINARY_BITS     96

//////////////////////////////////// itek_tatagid /////////////////////////////////////////
#define TATAGID_HEADER          0x35
#define TATAGID_EPC_CHARS       24
#define TATAGID_BINARY_BITS     96


//////////////////////////////////////// itek_non-standard //////////////////////////////////////
#define ALGO_NAME_RETAIL_GTIN       "retailgtin"
#define ALGO_NAME_TATA_GID          "tatagid"
#define ALGO_NAME_ITEK_NON_STD      "iteknonstandard"



/* ======================== CONSTANTS ======================== */
// Base character sets
#define BASE62_CHARS "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
#define BASE75_CHARS "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*=?-_+"

// Encoding limits and headers
#define ITEK_NON_STD_SERIAL_NUMBER_LIMIT 67108863  // 2^26 - 1
#define ITEK_NON_STD_96_HEADER  "10111011"
#define ITEK_NON_STD_128_HEADER "10111101"

// Mode definitions (2-bit)
#define MODE_NUMERIC 0b00
#define MODE_BASE62  0b01
#define MODE_BASE75  0b10

// Messages
#define ENCODE_SUCCESS_MESSAGE "Encoded successfully"
#define DECODE_SUCCESS_MESSAGE "Decoded successfully"
#define SERIAL_NUMBER_LIMIT_MESSAGE "Serial Number limit exceeded; cannot encode further."
#define INVALID_MODE_MESSAGE "Invalid mode"
#define UNKNOWN_HEADER_MESSAGE "Unknown header"
#define LARGE_INPUT_DATA_MESSAGE "Encoding Failed. Invalid EAN. Please check EAN type or contact admin."
#define INVALID_EPC_MESSAGE "Invalid epc"
#define INVALID_INPUT_MESSAGE "Invalid input."
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/* ============================================================================
 * PARTITION TABLE STRUCTURE
 * ============================================================================
 */

typedef struct {
    int gcp_bits;
    int item_bits;
} PartitionInfo;

/* ======================== RESULT STRUCTURES ======================== */
typedef struct {
    bool status;
    char message[256];
    char epc[256];
} EncodeResult;

typedef struct {
    bool status;
    char message[256];
    char barcode[256];
    char serialNumber[256];
} DecodeResult;

// #ifdef __cplusplus
// }
// #endif

#endif /* ITEK_COMMON_H */
