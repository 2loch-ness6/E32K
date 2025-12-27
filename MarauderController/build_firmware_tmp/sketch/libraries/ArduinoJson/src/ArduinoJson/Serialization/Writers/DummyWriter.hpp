#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Serialization/Writers/DummyWriter.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Namespace.hpp>

namespace ARDUINOJSON_NAMESPACE {

class DummyWriter {
 public:
  size_t write(uint8_t) {
    return 1;
  }

  size_t write(const uint8_t*, size_t n) {
    return n;
  }
};
}  // namespace ARDUINOJSON_NAMESPACE
