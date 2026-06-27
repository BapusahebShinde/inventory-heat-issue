/*
 * TATAGID Header File
 * TATAGID Encoder/Decoder
 */

#ifndef ITEK_TATAGID_H
#define ITEK_TATAGID_H

#include <stdint.h>
#include <stdbool.h>
#include "itek_common.h"


/* ============================================================================
 * FUNCTION DECLARATIONS
 * ============================================================================
 */

int  tatagid_calculate_check_digit(const char *gtin13);
bool tatagid_validate_gtin14(const char *gtin14);
bool tatagid_validate_epc_hex(const char *epc_hex);
int  tatagid_determine_partition(const char *gcp);
PartitionInfo tatagid_get_partition_info(int partition);

/**
 * Encode GTIN-14 + serial (string) into SGTIN-96 EPC
 */
EncodeResult tatagid_encode(const char *gtin14, const char *serial);

/**
 * Decode SGTIN-96 EPC
 */
DecodeResult tatagid_decode(const char *epc_hex);

bool tatagid_gtin14_to_gtin12(const char *gtin14, char *gtin12_out);
bool tatagid_gtin12_to_gtin14(const char *gtin12, char *gtin14_out);

#endif /* tatagid_H */
