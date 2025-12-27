#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Strings/IsString.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Polyfills/type_traits.hpp>
#include <ArduinoJson/Strings/StringAdapter.hpp>

namespace ARDUINOJSON_NAMESPACE {

template <typename T, typename Enable = void>
struct IsString : false_type {};

template <typename T>
struct IsString<
    T, typename make_void<typename StringAdapter<T>::AdaptedString>::type>
    : true_type {};

}  // namespace ARDUINOJSON_NAMESPACE
