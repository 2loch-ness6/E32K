#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Polyfills/type_traits/is_same.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include "integral_constant.hpp"

namespace ARDUINOJSON_NAMESPACE {

// A meta-function that returns true if types T and U are the same.
template <typename T, typename U>
struct is_same : false_type {};

template <typename T>
struct is_same<T, T> : true_type {};
}  // namespace ARDUINOJSON_NAMESPACE
