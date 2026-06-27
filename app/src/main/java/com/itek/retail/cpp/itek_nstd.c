/**
 * i-Tek RFID - Non-Standard Encoder/Decoder Library
 * 
 * This module provides functionality for:
 * - Encoding barcode data with serial numbers into EPC hex strings
 * - Decoding EPC hex strings back to barcode and serial numbers
 * - Supporting multiple data formats: Numeric, Base62, and Base75
 * - Adaptive storage: 96-bit and 128-bit container support
 *
 * Author: Saurav Jain
 */

#include <stdio.h>
#include <ctype.h>
#include "itek_nstd.h"



/* ======================== BIGINT CORE ======================== */

void bigint_init(BigInt *n)
{
    n->capacity = 4;
    n->size = 1;
    n->digits = calloc(n->capacity, sizeof(uint32_t));
}

void bigint_free(BigInt *n)
{
    free(n->digits);
}

static void bigint_ensure_capacity(BigInt *n, size_t needed)
{
    if (needed <= n->capacity) return;
    while (n->capacity < needed) n->capacity *= 2;
    n->digits = realloc(n->digits, n->capacity * sizeof(uint32_t));
}

void bigint_mul_small(BigInt *n, uint32_t m)
{
    uint64_t carry = 0;
    for (size_t i = 0; i < n->size; i++) {
        uint64_t v = (uint64_t)n->digits[i] * m + carry;
        n->digits[i] = v % BIGINT_BASE;
        carry = v / BIGINT_BASE;
    }
    if (carry) {
        bigint_ensure_capacity(n, n->size + 1);
        n->digits[n->size++] = carry;
    }
}

void bigint_add_small(BigInt *n, uint32_t a)
{
    uint64_t carry = a;
    for (size_t i = 0; i < n->size && carry; i++) {
        uint64_t v = (uint64_t)n->digits[i] + carry;
        n->digits[i] = v % BIGINT_BASE;
        carry = v / BIGINT_BASE;
    }
    if (carry) {
        bigint_ensure_capacity(n, n->size + 1);
        n->digits[n->size++] = carry;
    }
}

int bigint_bit_length(const BigInt *n)
{
    uint32_t top = n->digits[n->size - 1];
    int bits = 0;
    while (top) { bits++; top >>= 1; }
    return bits + (n->size - 1) * 30;
}

/* ======================== BIGINT CONVERSIONS ======================== */

bool bigint_from_base(const char *input, int base, const char *chars, BigInt *out)
{
    bigint_init(out);
    for (int i = 0; input[i]; i++) {
        const char *p = strchr(chars, input[i]);
        if (!p) return false;
        bigint_mul_small(out, base);
        bigint_add_small(out, (uint32_t)(p - chars));
    }
    return true;
}

void bigint_from_binary(const char *binary, BigInt *out)
{
    bigint_init(out);
    for (int i = 0; binary[i]; i++) {
        bigint_mul_small(out, 2);
        if (binary[i] == '1') bigint_add_small(out, 1);
    }
}

void bigint_to_binary(const BigInt *n, int bits, char *output)
{
    BigInt temp;
    bigint_init(&temp);
    bigint_ensure_capacity(&temp, n->size);
    memcpy(temp.digits, n->digits, n->size * sizeof(uint32_t));
    temp.size = n->size;

    for (int i = bits - 1; i >= 0; i--) {
        uint64_t carry = 0;
        for (int j = (int)temp.size - 1; j >= 0; j--) {
            uint64_t cur = temp.digits[j] + carry * BIGINT_BASE;
            temp.digits[j] = cur / 2;
            carry = cur % 2;
        }
        output[i] = carry ? '1' : '0';
        while (temp.size > 1 && temp.digits[temp.size - 1] == 0)
            temp.size--;
    }
    output[bits] = '\0';
    bigint_free(&temp);
}

void bigint_to_base(BigInt *n, int base, const char *chars, char *output)
{
    if (n->size == 1 && n->digits[0] == 0) {
        output[0] = chars[0];
        output[1] = '\0';
        return;
    }

    char buf[256];
    int idx = 0;

    while (!(n->size == 1 && n->digits[0] == 0)) {
        uint64_t rem = 0;
        for (int i = (int)n->size - 1; i >= 0; i--) {
            uint64_t cur = n->digits[i] + rem * BIGINT_BASE;
            n->digits[i] = cur / base;
            rem = cur % base;
        }
        buf[idx++] = chars[rem];
        while (n->size > 1 && n->digits[n->size - 1] == 0)
            n->size--;
    }

    for (int i = 0; i < idx; i++)
        output[i] = buf[idx - 1 - i];
    output[idx] = '\0';
}

/* ======================== HELPERS ======================== */

int detect_mode(const char *input)
{
    bool alpha = false, special = false;
    for (int i = 0; input[i]; i++) {
        if (isdigit(input[i])) continue;
        else if (isalpha(input[i])) alpha = true;
        else special = true;
    }
    if (!alpha && !special) return MODE_NUMERIC;
    if (!special) return MODE_BASE62;
    return MODE_BASE75;
}

void binary_to_bytes(const char *binary, unsigned char *bytes, int *count)
{
    int len = strlen(binary);
    *count = (len + 7) / 8;
    for (int i = 0; i < *count; i++) {
        bytes[i] = 0;
        for (int j = 0; j < 8; j++) {
            int idx = i * 8 + j;
            if (idx < len && binary[idx] == '1')
                bytes[i] |= (1 << (7 - j));
        }
    }
}

void bytes_to_binary(const unsigned char *bytes, int count, char *binary)
{
    int pos = 0;
    for (int i = 0; i < count; i++)
        for (int j = 7; j >= 0; j--)
            binary[pos++] = ((bytes[i] >> j) & 1) ? '1' : '0';
    binary[pos] = '\0';
}

void bytes_to_hex(const unsigned char *bytes, int count, char *hex)
{
    for (int i = 0; i < count; i++)
        sprintf(hex + i * 2, "%02X", bytes[i]);
}

void hex_string_to_bytes(const char *hex, unsigned char *bytes, int *count)
{
    *count = strlen(hex) / 2;
    for (int i = 0; i < *count; i++)
        sscanf(hex + i * 2, "%2hhX", &bytes[i]);
}


/*========================= get_barcode_len_bits ========================*/
int get_barcode_len_bits(const char *input)
{
    if (!input) return -1;

    int mode = detect_mode(input);
    BigInt data;
    bool valid = true;

    if (mode == MODE_NUMERIC)
        valid = bigint_from_base(input, 10, "0123456789", &data);
    else if (mode == MODE_BASE62)
        valid = bigint_from_base(input, 62, BASE62_CHARS, &data);
    else
        valid = bigint_from_base(input, 75, BASE75_CHARS, &data);

    if (!valid) {
        return -1;
    }

    int data_bits = bigint_bit_length(&data);
    int total_bits = (data_bits <= 60) ? 96 : (data_bits <= 92 ? 128 : 0);
    if (!total_bits) {
        return -1;
    }

    bigint_free(&data);

    return total_bits;
}


/* ======================== ENCODE ======================== */

EncodeResult itek_nstd_encode(const char *input, const char *serial_number)
{
    EncodeResult r = { false, "", "" };

    uint32_t serial = (uint32_t)strtoul(serial_number, NULL, 10);
    if (serial > ITEK_NON_STD_SERIAL_NUMBER_LIMIT) {
        strcpy(r.message, SERIAL_NUMBER_LIMIT_MESSAGE);
        return r;
    }

    int mode = detect_mode(input);
    BigInt data;
    bool valid = true;

    if (mode == MODE_NUMERIC)
        valid = bigint_from_base(input, 10, "0123456789", &data);
    else if (mode == MODE_BASE62)
        valid = bigint_from_base(input, 62, BASE62_CHARS, &data);
    else
        valid = bigint_from_base(input, 75, BASE75_CHARS, &data);

    if (!valid) {
        strcpy(r.message, INVALID_INPUT_MESSAGE);
        return r;
    }

    int data_bits = bigint_bit_length(&data);
    int total_bits = (data_bits <= 60) ? 96 : (data_bits <= 92 ? 128 : 0);
    if (!total_bits) {
        strcpy(r.message, LARGE_INPUT_DATA_MESSAGE);
        return r;
    }

    char bitstream[256] = "";
    strcat(bitstream, total_bits == 96 ? ITEK_NON_STD_96_HEADER : ITEK_NON_STD_128_HEADER);

    char mode_bits[3] = {
        (mode & 2) ? '1' : '0',
        (mode & 1) ? '1' : '0',
        '\0'
    };
    strcat(bitstream, mode_bits);

    char data_bin[128];
    bigint_to_binary(&data, total_bits == 96 ? 60 : 92, data_bin);
    strcat(bitstream, data_bin);

    char serial_bin[32];
    for (int i = 25; i >= 0; i--)
        serial_bin[25 - i] = ((serial >> i) & 1) ? '1' : '0';
    serial_bin[26] = '\0';
    strcat(bitstream, serial_bin);

    unsigned char bytes[32];
    int byte_count;
    binary_to_bytes(bitstream, bytes, &byte_count);
    bytes_to_hex(bytes, byte_count, r.epc);

    r.status = true;
    strcpy(r.message, ENCODE_SUCCESS_MESSAGE);
    bigint_free(&data);
    return r;
}

/* ======================== DECODE ======================== */

DecodeResult itek_nstd_decode(const char *encoded)
{
    DecodeResult r = { false, "", "", "" };

    if (strlen(encoded) != 24 && strlen(encoded) != 32) {
        strcpy(r.message, INVALID_EPC_MESSAGE);
        return r;
    }

    unsigned char bytes[32];
    int byte_count;
    hex_string_to_bytes(encoded, bytes, &byte_count);

    char binary[256];
    bytes_to_binary(bytes, byte_count, binary);

    char header[9];
    strncpy(header, binary, 8);
    header[8] = '\0';

    int mode = ((binary[8] - '0') << 1) | (binary[9] - '0');

    int data_len = strcmp(header, ITEK_NON_STD_96_HEADER) == 0 ? 60 :
                   strcmp(header, ITEK_NON_STD_128_HEADER) == 0 ? 92 : -1;
    if (data_len < 0) {
        strcpy(r.message, UNKNOWN_HEADER_MESSAGE);
        return r;
    }

    char data_bits[128];
    strncpy(data_bits, binary + 10, data_len);
    data_bits[data_len] = '\0';

    char serial_bits[27];
    strncpy(serial_bits, binary + 10 + data_len, 26);
    serial_bits[26] = '\0';

    BigInt data;
    bigint_from_binary(data_bits, &data);

    if (mode == MODE_NUMERIC)
        bigint_to_base(&data, 10, "0123456789", r.barcode);
    else if (mode == MODE_BASE62)
        bigint_to_base(&data, 62, BASE62_CHARS, r.barcode);
    else
        bigint_to_base(&data, 75, BASE75_CHARS, r.barcode);

    uint32_t serial = 0;
    for (int i = 0; i < 26; i++)
        serial = (serial << 1) | (serial_bits[i] - '0');

    sprintf(r.serialNumber, "%u", serial);
    strcpy(r.message, DECODE_SUCCESS_MESSAGE);
    r.status = true;

    bigint_free(&data);
    return r;
}
