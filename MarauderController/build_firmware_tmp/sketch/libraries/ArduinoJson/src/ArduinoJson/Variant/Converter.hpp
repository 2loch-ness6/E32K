#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Variant/Converter.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

namespace ARDUINOJSON_NAMESPACE {

template <typename T, typename Enable = void>
struct Converter;

// clang-format off
template <typename T1, typename T2>
class InvalidConversion;  // Error here? See https://arduinojson.org/v6/invalid-conversion/
// clang-format on

template <typename T>
struct ConverterNeedsWriteableRef;

}  // namespace ARDUINOJSON_NAMESPACE
