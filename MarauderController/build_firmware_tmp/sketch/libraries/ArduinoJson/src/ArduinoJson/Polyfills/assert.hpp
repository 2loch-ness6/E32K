#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Polyfills/assert.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Configuration.hpp>

#if ARDUINOJSON_DEBUG
#  include <assert.h>
#  define ARDUINOJSON_ASSERT(X) assert(X)
#else
#  define ARDUINOJSON_ASSERT(X) ((void)0)
#endif
