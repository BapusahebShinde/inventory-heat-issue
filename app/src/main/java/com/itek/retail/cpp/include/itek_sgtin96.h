/*
 * SGTIN-96 Header File
 * GS1 SGTIN-96 Encoder/Decoder - C99 Implementation
 */

#ifndef ITEK_SGTIN96_H
#define ITEK_SGTIN96_H

#include <stdint.h>
#include <stdbool.h>
#include "itek_common.h"


/* ============================================================================
 * FUNCTION DECLARATIONS
 * ============================================================================
 */

int  sgtin96_calculate_check_digit(const char *gtin13);
static bool sgtin96_validate_gtin14(const char *in, char *out14);
bool sgtin96_validate_epc_hex(const char *epc_hex);
int  sgtin96_determine_partition(const char *gcp);
PartitionInfo sgtin96_get_partition_info(int partition);

/**
 * Encode GTIN-14 + serial (string) into SGTIN-96 EPC
 */
EncodeResult sgtin96_encode(const char *gtin14, const char *serial);

/**
 * Decode SGTIN-96 EPC
 */
DecodeResult sgtin96_decode(const char *epc_hex);

bool sgtin96_gtin14_to_gtin12(const char *gtin14, char *gtin12_out);
bool sgtin96_gtin12_to_gtin14(const char *gtin12, char *gtin14_out);

#endif /* SGTIN96_H */
