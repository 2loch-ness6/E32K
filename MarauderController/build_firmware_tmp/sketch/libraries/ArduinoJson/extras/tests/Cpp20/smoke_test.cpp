#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/Cpp20/smoke_test.cpp"
#include <ArduinoJson.h>

#include <catch.hpp>
#include <string>

TEST_CASE("C++20 smoke test") {
  StaticJsonDocument<128> doc;

  deserializeJson(doc, "{\"hello\":\"world\"}");
  REQUIRE(doc["hello"] == "world");

  std::string json;
  serializeJson(doc, json);
  REQUIRE(json == "{\"hello\":\"world\"}");
}
