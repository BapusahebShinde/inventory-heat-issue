#ifndef MAIN_H
#define MAIN_H

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "itek_common.h"


// #ifdef __cplusplus
// extern "C" {
// #endif

/* Getter for library version */
const char* get_itek_library_version();

/* Check if input is a valid Non-standard-96 barcode */
int is_valid_NS96_barcode(const char *input);
int is_validate_gtin14(const char *in, char *out14);
int is_validate_sgtin96_epc_hex(const char *epc);

/* Unified API for users */
int itek_encode(const char *barcode, const char *serial, EncodeResult *result, const char *algo_name);
int itek_decode(const char *encoded_data, DecodeResult *result);

// #ifdef __cplusplus
// }
// #endif

#endif /* MAIN_H */
