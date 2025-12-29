#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/stickcLED.h"
#pragma once

#ifndef stickcLED_H
#define stickcLED_H

#include "configs.h"
#include "settings.h"

#include <Arduino.h>


extern Settings settings_obj;

class stickcLED {

    public:
        void RunSetup();
        void main();
        void attackLED();
        void sniffLED();
        void offLED();
};

#endif  /* stickcLED_H */
