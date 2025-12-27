#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/src/ArduinoJson/Serialization/measure.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <ArduinoJson/Serialization/Writers/DummyWriter.hpp>
#include <ArduinoJson/Variant/VariantFunctions.hpp>

namespace ARDUINOJSON_NAMESPACE {

template <template <typename> class TSerializer>
size_t measure(VariantConstRef source) {
  DummyWriter dp;
  TSerializer<DummyWriter> serializer(dp);
  return variantAccept(VariantAttorney::getData(source), serializer);
}

}  // namespace ARDUINOJSON_NAMESPACE
