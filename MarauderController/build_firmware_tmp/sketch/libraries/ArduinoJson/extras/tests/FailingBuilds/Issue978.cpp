#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/FailingBuilds/Issue978.cpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#include <ArduinoJson.h>

struct Stream {};

int main() {
  Stream* stream = 0;
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, stream);
}
