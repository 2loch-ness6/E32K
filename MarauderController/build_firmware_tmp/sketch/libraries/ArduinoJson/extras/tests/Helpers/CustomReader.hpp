#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/ArduinoJson/extras/tests/Helpers/CustomReader.hpp"
// ArduinoJson - https://arduinojson.org
// Copyright © 2014-2022, Benoit BLANCHON
// MIT License

#pragma once

#include <sstream>

class CustomReader {
  std::stringstream _stream;

 public:
  CustomReader(const char* input) : _stream(input) {}

  int read() {
    return _stream.get();
  }

  size_t readBytes(char* buffer, size_t length) {
    _stream.read(buffer, static_cast<std::streamsize>(length));
    return static_cast<size_t>(_stream.gcount());
  }

 private:
  CustomReader(const CustomReader&);
};
