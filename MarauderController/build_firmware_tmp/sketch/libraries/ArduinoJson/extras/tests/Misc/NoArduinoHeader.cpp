#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/Misc/NoArduinoHeader.cpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#define ARDUINO 1
#define ARDUINOJSON_ENABLE_PROGMEM 0
#define ARDUINOJSON_ENABLE_ARDUINO_STRING 0
#define ARDUINOJSON_ENABLE_ARDUINO_STREAM 0
#define ARDUINOJSON_ENABLE_ARDUINO_PRINT 0
#include <ArduinoJson.h>

#include <catch.hpp>

TEST_CASE("Arduino.h") {
#ifdef ARDUINO_H_INCLUDED
  FAIL("Arduino.h should not be included");
#else
  INFO("Arduino.h not included");
#endif
}
