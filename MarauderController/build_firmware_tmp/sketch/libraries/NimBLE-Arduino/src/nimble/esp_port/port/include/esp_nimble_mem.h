#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/NimBLE-Arduino/src/nimble/esp_port/port/include/esp_nimble_mem.h"
/*
 * SPDX-FileCopyrightText: 2015-2021 Espressif Systems (Shanghai) CO LTD
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#ifndef __ESP_NIMBLE_MEM_H__
#define __ESP_NIMBLE_MEM_H__

#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

void *nimble_platform_mem_malloc(size_t size);
void *nimble_platform_mem_calloc(size_t n, size_t size);
void nimble_platform_mem_free(void *ptr);

#ifdef __cplusplus
}
#endif

#endif /* __ESP_NIMBLE_MEM_H__ */
