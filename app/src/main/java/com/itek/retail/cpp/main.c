#include <stdio.h>
#include <string.h>
#include <ctype.h>


/* Library headers */
#include "include/itek_nstd.h"
#include "include/itek_sgtin96.h"
// #include "itek_tatagid.h"


/*
* getter for library version
* @return library version in string format
*/
//library version defined in itek_common.h
const char* get_itek_library_version() {
    return Lib_Version;
}

int is_valid_NS96_barcode(const char *input)
{
   int ret_status = get_barcode_len_bits(input);
   return (ret_status == 96) ? 1 : 0;
}

/* -------------------------------------------------
 * Helper: check if barcode is GTIN-14 (numeric, 14 digits)
 * ------------------------------------------------- */

/*int sgtin96_calculate_check_digit(const char *gtin13) {
    if (!gtin13 || 0 > strlen(gtin13) || strlen(gtin13) > 14)
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
}*/

/* Normalize + validate GTIN-14 */
int is_validate_gtin14(const char *in, char *out14) {
    if (!in) {
        // printf("Invalid input: NULL\n");
        return 0;
    }

    int len = strlen(in);
    if (len == 0 || len > 14) { 
        // printf("Invalid input length: %d\n", len);
        return 0;
    }

    for (int i = 0; i < len; i++)
        if (!isdigit(in[i])) {
            // printf("Invalid character in input: %c\n", in[i]);
            return 0;
        }
        

    memset(out14, '0', 14);
    memcpy(out14 + (14 - len), in, len);
    out14[14] = '\0';

    char gtin13[14];
    memcpy(gtin13, out14, 13);
    gtin13[13] = '\0';

    int expected = sgtin96_calculate_check_digit(gtin13);
    int actual = out14[13] - '0';
    // printf("GTIN-14 Check Digit: Expected %d, Actual %d\n", expected, actual);

    return expected >= 0 && expected == actual ? 1 : 0;
}


int is_validate_sgtin96_epc_hex(const char *epc) {
    if (!epc || strlen(epc) != SGTIN96_EPC_CHARS)
        return  0;
    for (int i = 0; i < SGTIN96_EPC_CHARS; i++)
        if (!isxdigit(epc[i]))
            return 0;
    return 1;
}

/* -------------------------------------------------
 * Unified Encode
 * ------------------------------------------------- */
int itek_encode(const char *barcode, const char *serial, EncodeResult *result, const char *algo_name)
{
    if (!barcode || !serial) {
        return -1;
    }

    // if (is_gtin14(barcode)) {
    char norm_gtin14[15];
    // printf("Checking GTIN : %d\n", is_validate_gtin14(barcode, norm_gtin14));
    if (is_validate_gtin14(barcode, norm_gtin14) && strcmp(algo_name, ALGO_NAME_RETAIL_GTIN) == 0) {
        printf("Encoding Barcode With SGTIN-96\n");
        /* Use SGTIN-96 */
        *result = sgtin96_encode(barcode, serial);
        return result->status ? 0 : -1;
    } else if (strcmp(algo_name, ALGO_NAME_ITEK_NON_STD) == 0) {
        printf("Encoding Barcode With i-Tek Non-standard\n");
        /* Use i-Tek Non-standard */
        *result = itek_nstd_encode(barcode, serial);
        return result->status ? 0 : -1;
    } else if (strcmp(algo_name, ALGO_NAME_TATA_GID) == 0) {
        // printf("Encoding Barcode With TATAGID\n");
        /* Use TATAGID */
        printf("TATAGID Encoding Is Not Yet Implemented.\n");
        // *result = tatagid_encode(barcode, serial);
        // return result->status ? 0 : -1;
        return 0;
    } else {
        if (is_validate_gtin14(barcode, norm_gtin14)) {
            printf("Encoding Barcode With SGTIN-96\n");
            /* Use SGTIN-96 */
            *result = sgtin96_encode(barcode, serial);
            return result->status ? 0 : -1;
        } else {
            printf("Encoding Barcode With Non-standard\n");
            /* Use Non-standard */
            *result = itek_nstd_encode(barcode, serial);
            return result->status ? 0 : -1;
        }
    }
}

/* -------------------------------------------------
 * Unified Decode
 * ------------------------------------------------- */
int itek_decode(const char *encoded_data, DecodeResult *result)
{
    if (!encoded_data) {
        return -1;
    }

    /*
     * Simple rule:
     * - SGTIN-96 EPC is usually 96 bits = 24 hex chars
     * - Otherwise assume Non-standard
     */
    if (strlen(encoded_data) == 24 && (encoded_data[0] == '3') && (encoded_data[1] == '0')) {
        if (is_validate_sgtin96_epc_hex(encoded_data)) {
            printf("Decoding Barcode With SGTIN-96\n");
            /* Likely SGTIN-96 */
            *result = sgtin96_decode(encoded_data);
            return result->status ? 0 : -1;
        }
        else {
            printf("Decoding Barcode With Non-standard\n");
            /* Non-standard */
            *result = itek_nstd_decode(encoded_data);
            return result->status ? 0 : -1;
        }
    } else {
        printf("Decoding Barcode With Non-standard\n");
        /* Non-standard */
        *result = itek_nstd_decode(encoded_data);
        return result->status ? 0 : -1;
    }
}
