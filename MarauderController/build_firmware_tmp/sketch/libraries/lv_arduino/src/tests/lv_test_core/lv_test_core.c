#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/lv_arduino/src/tests/lv_test_core/lv_test_core.c"
/**
 * @file lv_test_core.c
 *
 */

/*********************
 *      INCLUDES
 *********************/
#include "../lv_test_assert.h"

#if LV_BUILD_TEST
#include "lv_test_core.h"
#include "lv_test_obj.h"
#include "lv_test_style.h"

/*********************
 *      DEFINES
 *********************/

/**********************
 *      TYPEDEFS
 **********************/

/**********************
 *  STATIC PROTOTYPES
 **********************/

/**********************
 *  STATIC VARIABLES
 **********************/

/**********************
 *      MACROS
 **********************/

/**********************
 *   GLOBAL FUNCTIONS
 **********************/

void lv_test_core(void)
{
    lv_test_print("");
    lv_test_print("*******************");
    lv_test_print("Start lv_core tests");
    lv_test_print("*******************");

    lv_test_obj();
    lv_test_style();
}


/**********************
 *   STATIC FUNCTIONS
 **********************/
#endif
