#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/JsonArray/createNested.cpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#include <ArduinoJson.h>
#include <catch.hpp>

TEST_CASE("JsonArray basics") {
  DynamicJsonDocument doc(4096);
  JsonArray array = doc.to<JsonArray>();

  SECTION("CreateNestedArray") {
    JsonArray arr = array.createNestedArray();
    REQUIRE(arr == array[0].as<JsonArray>());
  }

  SECTION("CreateNestedObject") {
    JsonObject obj = array.createNestedObject();
    REQUIRE(obj == array[0].as<JsonObject>());
  }
}
