# Marauder Versions
There are several different versions of Marauder I have developed. Each Marauder has its own range of capabilities, some better than others. The following list describes each official version of the ESP32 Marauder.  

- [ESP32 Marauder v4](esp32-marauder-v4)
- [ESP32 Marauder v6](#esp32-marauder-v6)
- [ESP32 Marauder Kit](#esp32-marauder-kit)
- [ESP32 Marauder v6.1](#esp32-marauder-v6-1)
- [ESP32 Marauder Mini](#esp32-marauder-mini)
- [ESP32 LDDB](#esp32-lddb)
- [Dev Board Pro](marauder-dev-board-pro)
- [Flipper Zero BFFB](bffb)
- [ESP32 Marauder v7](#esp32-marauder-v7)

## Big Table of Features and stuff
|                | v4 (OG)            | v6                 | v6.1               | Kit                | Mini               | Flipper                                | v7                 |
| -------------- | ------------------ | ------------------ | ------------------ | ------------------ | ------------------ | -------------------------------------- | ------------------ |
| Display        | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                                    | :heavy_check_mark: |
| Touch          | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                | :x:                                    | :x:                |
| Tactile Switch | :x:                | :x:                | :x:                | :x:                | :heavy_check_mark: | :x:                                    | :heavy_check_mark: |
| SD Card        | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:(with mod)           | :heavy_check_mark: |
| Battery        | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:(with flipper)       | :heavy_check_mark: |
| WiFi           | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| Bluetooth      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                                    | :heavy_check_mark: |
| Command Line   | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| Packet Monitor | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                | :x:                                    | :x:                |
| Onscreen Keybd | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                | :x:                                    | :x:                |
| Joystick Keybd | :x:                | :x:                | :x:                | :x:                | :heavy_check_mark: | :x:                                    | :heavy_check_mark: |
| PMKID Capture  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| Deauth         | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| Raw Capture    | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| AP Select      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                     | :heavy_check_mark: |
| Manual SSID    | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:(CLI Only)           | :heavy_check_mark: |
| SD Update      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:(with mod)           | :heavy_check_mark: |
| Status LED     | :x:                | :x:                | :x:                | :x:                | :x:                | :heavy_check_mark:                     | :x:                |
| HID attacks    | :x:                | :x:                | :x:                | :x:                | :x:                | :x:                                    | :x:                |
| ESP8266 co-mcu | :x:                | :x:                | :x:                | :x:                | :x:                | :x:                                    | :x:                |
| Onboard GPS    | :x:                | :x:                | :heavy_check_mark: | :x:                | :heavy_check_mark: | :x:                                    | :heavy_check_mark: |
| Battery Monitor| :heavy_check_mark: (with mod)| :heavy_check_mark: (with mod)| :heavy_check_mark: (with mod)| :heavy_check_mark: (with mod)| :heavy_check_mark: (with mod)| :x: | :heavy_check_mark: |

## ESP32 Marauder v6
Counter to what the name implies, this is the second official iteration of the ESP32 Marauder. The main differences between this iteration and its predecessor is its use of a push-in push-out micro SD card slot, single PCB design with the TFT fixed directly to the PCB, 18650 Battery holder, and an improved full enclosure. Aside from those aspects, the overall functionality of this version remains the same as the previous.  

<p align="left">
  <img alt="ESP32 WROOM-32U" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/IMG_0426.JPG?raw=true" width="500">
</p>


## ESP32 Marauder Kit
It's a kit
<p align="left">
  <img alt="ESP32 WROOM-32U" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/IMG_3491%20-%20Copy.jpg?raw=true" width="500">
</p>

## ESP32 Marauder v6.1
The ESP32 Marauder v6.1/v6.2 is an official full-sized Marauder end-user device, enabled with a touch-screen. It features on-board GPS, 18650 battery management, and an external 2.4ghz antenna with the option to mount an external GPS antenna as a replacement for the included patch antenna. The v6.X has a 2.8" ILI9341 TFT screen with touch controller, enabling the user to navigate the various functions with touch. The v6.X is fully controllable over Serial via the Marauder CLI.

### Modifications


#### Battery Fuel Gauge


## ESP32 Marauder Mini
The ESP32 Marauder Mini is an official discrete-sized Marauder end-user device, enabled with a 5-way tactile switch. It features on-board GPS, LiPo battery management, and an external 2.4ghz antenna with the option to mount an external GPS antenna as a replacement for the included patch antenna. The Mini has a 1.44" ST7735 screen and a 5-way tactile switch, enabling the user to navigate the various functions with a joystick. The Mini is fully controllable over Serial via the Marauder CLI.

### Modifications


#### Battery Fuel Gauge


## ESP32 LDDB


## ESP32 Marauder v7
The ESP32 Marauder v7 is an official mid-sized Marauder end-user device, enabled with a 5-way tactile switch. It features on-board GPS, LiPo battery management, battery fuel gauge, and external 2.4ghz and GPS antennas. The v7 has a 2.4" ILI9341 screen and a 5-way tactile switch, enabling the user to navigate the various functions with a joystick. The v7 is fully controllable over Serial via the Marauder CLI.

##### Firmware Features
The Marauder v7 supports all of the WiFi and Bluetooth functions created in the Marauder firmware. For a full list of features and their descriptions and usage, please see [Applications](applications) and [Workflow Examples](workflow-examples).

##### Powering The Device
The Marauder v7 can receive power from two sources; USB, and LiPo pack. USB power must be 5V. If using a LiPo battery, it must be a 3.7V 1S pack with a 2mm pitch 2-pin JST connector. Connect the LiPo pack to the on-board JST connector. **Ensure the polarity of the battery matches the marking on the Marauder v7 PCB.** With USB power and/or battery connected, toggle the power switch on the left side of the enclosure to the `ON` position. At this point, you should see the Marauder boot screen.

##### External 2.4GHz Antenna
The 2.4GHz antenna included with the Marauder v7 may be removed to reveal an SMA female connector. This connector will accept any 2.4GHz antenna with an SMA male connector. There is a marking on the enclosure under the antenna connector to indicate which antenna is to be placed.

##### External GPS Antenna
The GPS antenna included with the Marauder v7 may be removed to reveal an SMA female connector. This connector will accept any GPS/GNSS antenna with an SMA male connector. There is a marking on the enclosure under the antenna connector to indicate which antenna is to be placed.

##### Battery Fuel Gauge
Battery SOC is represented by a numerical value at the top right of the screen on the status bar