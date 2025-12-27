#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Polyfills/type_traits/remove_reference.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Namespace.hpp>

namespace ARDUINOJSON_NAMESPACE {

// A meta-function that return the type T without the reference modifier.
template <typename T>
struct remove_reference {
  typedef T type;
};
template <typename T>
struct remove_reference<T&> {
  typedef T type;
};
}  // namespace ARDUINOJSON_NAMESPACE
