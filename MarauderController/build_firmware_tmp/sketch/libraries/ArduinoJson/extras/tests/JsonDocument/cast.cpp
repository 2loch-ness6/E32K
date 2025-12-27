#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/JsonDocument/cast.cpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#include <ArduinoJson.h>
#include <catch.hpp>

#include <string>

TEST_CASE("Implicit cast to JsonVariant") {
  StaticJsonDocument<128> doc;

  doc["hello"] = "world";

  JsonVariant var = doc;

  CHECK(var.as<std::string>() == "{\"hello\":\"world\"}");
}
