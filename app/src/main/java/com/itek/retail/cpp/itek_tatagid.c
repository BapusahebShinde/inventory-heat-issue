/**
 * i-Tek RFID - TATAGID Encoder/Decoder Library
 *
 * Author: Saurav Jain
*/
 

#include "itek_tatagid.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* ============================================================================
 * PARTITION TABLE
 * ============================================================================
 */

static const PartitionInfo partition_table[] = {
    {40, 4}, {37, 7}, {34, 10}, {30, 14},
    {27, 17}, {24, 20}, {20, 24}
};

static const int gcp_lengths[] = {12,11,10,9,8,7,6};

/* ============================================================================
 * UTILITY FUNCTIONS
 * ============================================================================
 */

static void binary_to_hex(const char *binary, int bits, char *hex_out) {
    unsigned char bytes[12] = {0};
    int count = (bits + 7) / 8;

    for (int i = 0; i < bits; i += 8) {
        int val = 0;
        for (int j = 0; j < 8 && i + j < bits; j++)
            val = (val << 1) | (binary[i + j] - '0');
        bytes[i / 8] = val;
    }

    char *p = hex_out;
    for (int i = 0; i < count; i++)
        p += sprintf(p, "%02X", bytes[i]);
    *p = '\0';
}

static void hex_to_binary(const char *hex, int len, char *bin) {
    for (int i = 0; i < len; i++) {
        unsigned v;
        sscanf(&hex[i], "%1x", &v);
        for (int j = 3; j >= 0; j--)
            bin[i * 4 + (3 - j)] = '0' + ((v >> j) & 1);
    }
    bin[len * 4] = '\0';
}

static uint64_t extract_bits(const char *bin, int start, int len) {
    uint64_t v = 0;
    for (int i = 0; i < len; i++)
        v = (v << 1) | (bin[start + i] - '0');
    return v;
}

static void insert_bits(char *bin, int start, uint64_t val, int len) {
    for (int i = len - 1; i >= 0; i--)
        bin[start + len - 1 - i] = '0' + ((val >> i) & 1);
}

/* ============================================================================
 * GTIN NORMALIZATION + VALIDATION
 * ============================================================================
 */

int tatagid_calculate_check_digit(const char *gtin13) {
    if (!gtin13 || strlen(gtin13) != 13)
        return -1;

    int sum = 0;
    int weight = 3;

    for (int i = 12; i >= 0; i--) {
        if (!isdigit(gtin13[i]))
            return -1;

        sum += (gtin13[i] - '0') * weight;
        weight = (weight == 3) ? 1 : 3;
    }

    return (10 - (sum % 10)) % 10;
}

/* Normalize + validate GTIN-14 */
static bool normalize_gtin14(const char *in, char *out14) {
    if (!in)
        return false;

    int len = strlen(in);
    if (len == 0 || len > 14)
        return false;

    for (int i = 0; i < len; i++)
        if (!isdigit(in[i]))
            return false;

    memset(out14, '0', 14);
    memcpy(out14 + (14 - len), in, len);
    out14[14] = '\0';

    char gtin13[14];
    memcpy(gtin13, out14, 13);
    gtin13[13] = '\0';

    int expected = tatagid_calculate_check_digit(gtin13);
    int actual = out14[13] - '0';

    return expected >= 0 && expected == actual;
}

bool tatagid_validate_gtin14(const char *gtin14) {
    char tmp[15];
    return normalize_gtin14(gtin14, tmp);
}

bool tatagid_validate_epc_hex(const char *epc) {
    if (!epc || strlen(epc) != TATAGID_EPC_CHARS)
        return false;
    for (int i = 0; i < TATAGID_EPC_CHARS; i++)
        if (!isxdigit(epc[i]))
            return false;
    return true;
}

int tatagid_determine_partition(const char *gcp) {
    if (!gcp) return -1;
    for (int i = 0; i < 7; i++)
        if ((int)strlen(gcp) == gcp_lengths[i])
            return i;
    return -1;
}

PartitionInfo tatagid_get_partition_info(int p) {
    return (p < 0 || p > 6) ? (PartitionInfo){0,0} : partition_table[p];
}

/* ============================================================================
 * ENCODE
 * ============================================================================
 */

EncodeResult tatagid_encode(const char *gtin14, const char *serial) {
    EncodeResult r = {0};
    r.status = false;

    char norm_gtin14[15];
    if (!normalize_gtin14(gtin14, norm_gtin14)) {
        snprintf(r.message, sizeof(r.message), "Invalid GTIN-14");
        return r;
    }

    if (!serial || !*serial) {
        snprintf(r.message, sizeof(r.message), "Serial is empty");
        return r;
    }

    char *end;
    uint64_t serial_val = strtoull(serial, &end, 10);
    if (*end || serial_val == 0 || serial_val > 274877906943ULL) {
        snprintf(r.message, sizeof(r.message), "Invalid serial number");
        return r;
    }

    char gcp[13], item[6];
    strncpy(gcp, &norm_gtin14[1], 7);
    gcp[7] = '\0';
    strncpy(item, &norm_gtin14[8], 5);
    item[5] = '\0';

    int partition = tatagid_determine_partition(gcp);
    if (partition < 0) {
        snprintf(r.message, sizeof(r.message), "Invalid GCP length");
        return r;
    }

    PartitionInfo p = tatagid_get_partition_info(partition);

    char bin[97];
    memset(bin, '0', 96);
    bin[96] = '\0';

    int pos = 0;
    insert_bits(bin, pos, TATAGID_HEADER, 8); pos += 8;
    insert_bits(bin, pos, 1, 3); pos += 3;
    insert_bits(bin, pos, partition, 3); pos += 3;
    insert_bits(bin, pos, strtoull(gcp, NULL, 10), p.gcp_bits); pos += p.gcp_bits;
    insert_bits(bin, pos, strtoull(item, NULL, 10), p.item_bits); pos += p.item_bits;
    insert_bits(bin, pos, serial_val, 38);

    binary_to_hex(bin, 96, r.epc);

    r.status = true;
    snprintf(r.message, sizeof(r.message), "SGTIN-96 encoded successfully");
    return r;
}

/* ============================================================================
 * DECODE
 * ============================================================================
 */

DecodeResult tatagid_decode(const char *epc_hex) {
    DecodeResult r = {0};
    r.status = false;

    if (!tatagid_validate_epc_hex(epc_hex)) {
        snprintf(r.message, sizeof(r.message), "Invalid EPC hex");
        return r;
    }

    char bin[97];
    hex_to_binary(epc_hex, TATAGID_EPC_CHARS, bin);

    int pos = 8 + 3;
    int partition = extract_bits(bin, pos, 3);
    pos += 3;

    PartitionInfo p = tatagid_get_partition_info(partition);

    uint64_t gcp = extract_bits(bin, pos, p.gcp_bits); pos += p.gcp_bits;
    uint64_t item = extract_bits(bin, pos, p.item_bits); pos += p.item_bits;
    uint64_t serial = extract_bits(bin, pos, 38);

    char gtin13[14];
    snprintf(gtin13, sizeof(gtin13), "0%0*lu%05lu",
             gcp_lengths[partition], (unsigned long)gcp,
             (unsigned long)item);

    int cd = tatagid_calculate_check_digit(gtin13);
    snprintf(r.barcode, sizeof(r.barcode), "%s%c", gtin13, '0' + cd);
    snprintf(r.serialNumber, sizeof(r.serialNumber), "%lu", (unsigned long)serial);

    r.status = true;
    snprintf(r.message, sizeof(r.message), "SGTIN-96 decoded successfully");
    return r;
}

/* ============================================================================
 * GTIN CONVERSIONS
 * ============================================================================
 */

bool tatagid_gtin14_to_gtin12(const char *gtin14, char *out) {
    char norm[15];
    if (!normalize_gtin14(gtin14, norm))
        return false;

    strncpy(out, &norm[1], 12);
    out[12] = '\0';
    return true;
}

bool tatagid_gtin12_to_gtin14(const char *gtin12, char *out) {
    if (!gtin12 || strlen(gtin12) != 12)
        return false;

    char gtin13[14];
    snprintf(gtin13, sizeof(gtin13), "0%s", gtin12);
    int cd = tatagid_calculate_check_digit(gtin13);

    snprintf(out, 15, "%s%c", gtin13, '0' + cd);
    return true;
}
