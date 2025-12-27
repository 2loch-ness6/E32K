#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/FailingBuilds/variant_as_char.cpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#include <ArduinoJson.h>

// See issue #1498

int main() {
  DynamicJsonDocument doc(1024);
  doc["dummy"].as<char>();
}
