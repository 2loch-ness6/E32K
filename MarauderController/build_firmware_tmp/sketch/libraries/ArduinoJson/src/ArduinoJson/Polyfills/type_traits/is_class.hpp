#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Polyfills/type_traits/is_class.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include "declval.hpp"

namespace ARDUINOJSON_NAMESPACE {

template <typename T>
struct is_class {
 protected:  // <- to avoid GCC's "all member functions in class are private"
  typedef char Yes[1];
  typedef char No[2];

  template <typename U>
  static Yes& probe(void (U::*)(void));
  template <typename>
  static No& probe(...);

 public:
  static const bool value = sizeof(probe<T>(0)) == sizeof(Yes);
};

}  // namespace ARDUINOJSON_NAMESPACE
