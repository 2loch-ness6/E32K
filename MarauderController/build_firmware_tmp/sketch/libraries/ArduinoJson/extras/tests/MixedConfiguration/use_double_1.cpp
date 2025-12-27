#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/MixedConfiguration/use_double_1.cpp"
#define ARDUINOJSON_USE_DOUBLE 1
#include <ArduinoJson.h>

#include <catch.hpp>

TEST_CASE("ARDUINOJSON_USE_DOUBLE == 1") {
  DynamicJsonDocument doc(4096);
  JsonObject root = doc.to<JsonObject>();

  root["pi"] = 3.14;
  root["e"] = 2.72;

  std::string json;
  serializeJson(doc, json);

  REQUIRE(json == "{\"pi\":3.14,\"e\":2.72}");
}
