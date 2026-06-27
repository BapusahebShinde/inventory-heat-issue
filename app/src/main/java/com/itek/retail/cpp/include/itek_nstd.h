/**
 * Nonstandard RFID Tag Encoder/Decoder - Header File
 * 
 * This header provides the public API for encoding and decoding
 * RFID tag EAN/barcode data using the nonstandard iTEK format.
 */

#ifndef ITEK_NSTD_H
#define ITEK_NSTD_H

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "itek_common.h"

/* ======================== BIGINT ======================== */

#define BIGINT_BASE 1000000000U  // 10^9

typedef struct {
    uint32_t *digits;
    size_t size;
    size_t capacity;
} BigInt;

// /* ======================== CONSTANTS ======================== */

// // Base character sets
// #define BASE62_CHARS "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
// #define BASE75_CHARS "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*=?-_+"

// // Encoding limits and headers
// #define ITEK_NON_STD_SERIAL_NUMBER_LIMIT 67108863  // 2^26 - 1
// #define ITEK_NON_STD_96_HEADER  "10111011"
// #define ITEK_NON_STD_128_HEADER "10111101"

// // Mode definitions (2-bit)
// #define MODE_NUMERIC 0b00
// #define MODE_BASE62  0b01
// #define MODE_BASE75  0b10

// // Messages
// #define ENCODE_SUCCESS_MESSAGE "Encoded successfully"
// #define DECODE_SUCCESS_MESSAGE "Decoded successfully"
// #define SERIAL_NUMBER_LIMIT_MESSAGE "Serial Number limit exceeded; cannot encode further."
// #define INVALID_MODE_MESSAGE "Invalid mode"
// #define UNKNOWN_HEADER_MESSAGE "Unknown header"
// #define LARGE_INPUT_DATA_MESSAGE "Encoding Failed. Invalid EAN. Please check EAN type or contact admin."
// #define INVALID_EPC_MESSAGE "Invalid epc"
// #define INVALID_INPUT_MESSAGE "Invalid input."

// /* ======================== RESULT STRUCTURES ======================== */

// typedef struct {
//     bool status;
//     char message[256];
//     char epc[256];
// } EncodeResult;

// typedef struct {
//     bool status;
//     char message[256];
//     char barcode[256];
//     char serialNumber[256];
// } DecodeResult;

/*========================= get_barcode_len_bits ========================*/
int get_barcode_len_bits(const char *input);

/* ======================== PUBLIC API ======================== */

EncodeResult itek_nstd_encode(const char *input, const char *serial_number);
DecodeResult itek_nstd_decode(const char *encoded);

#endif /* ITEK_NSTD_H */
