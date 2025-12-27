#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Strings/Adapters/ArduinoString.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <Arduino.h>

#include <ArduinoJson/Strings/Adapters/RamString.hpp>
#include <ArduinoJson/Strings/StringAdapter.hpp>

namespace ARDUINOJSON_NAMESPACE {

template <typename T>
struct StringAdapter<
    T, typename enable_if<is_same<T, ::String>::value ||
                          is_same<T, ::StringSumHelper>::value>::type> {
  typedef SizedRamString AdaptedString;

  static AdaptedString adapt(const ::String& s) {
    return AdaptedString(s.c_str(), s.length());
  }
};

}  // namespace ARDUINOJSON_NAMESPACE
