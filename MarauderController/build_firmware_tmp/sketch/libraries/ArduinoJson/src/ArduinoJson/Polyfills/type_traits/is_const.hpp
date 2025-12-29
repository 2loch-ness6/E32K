#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Polyfills/type_traits/is_const.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include "integral_constant.hpp"

namespace ARDUINOJSON_NAMESPACE {

// A meta-function that return the type T without the const modifier
template <typename T>
struct is_const : false_type {};

template <typename T>
struct is_const<const T> : true_type {};
}  // namespace ARDUINOJSON_NAMESPACE
