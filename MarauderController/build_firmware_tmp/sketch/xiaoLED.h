#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/xiaoLED.h"
#pragma once

#ifndef xiaoLED_H
#define xiaoLED_H

#include "configs.h"
#include "settings.h"

#include <Arduino.h>

#define XIAO_LED_PIN 21

extern Settings settings_obj;

class xiaoLED {

    public:
        void RunSetup();
        void main();
        void attackLED();
        void sniffLED();
        void offLED();
};

#endif  /* xiaoLED_H */