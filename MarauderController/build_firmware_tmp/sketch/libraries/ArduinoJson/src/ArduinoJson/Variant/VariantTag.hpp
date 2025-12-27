#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Variant/VariantTag.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Namespace.hpp>

namespace ARDUINOJSON_NAMESPACE {

struct VariantTag {};

template <typename T>
struct IsVariant : is_base_of<VariantTag, T> {};

}  // namespace ARDUINOJSON_NAMESPACE
