# Flipper Zero WiFi Dev Board with Marauder

[![Build and Push](https://github.com/justcallmekoko/ESP32Marauder/actions/workflows/build_push.yml/badge.svg)](https://github.com/justcallmekoko/ESP32Marauder/actions/workflows/build_push.yml)

If at any moment you need further guidance, consider joining the discord server.  
<a href="https://discord.com/servers/willstunforfood-776211399918878760"><img src="https://discordapp.com/api/guilds/776211399918878760/widget.png?style=banner4" alt="JustCallMeKoko Discord"></a>

## Table of Consonants
- [Introduction](#introduction)
- [Installation](#installation)
  - [Flipper Zero Preparation](#flipper-zero-preparation)
  - [ESP32 Preparation](#esp32-preparation)
- [Having Issues?](#having-issues)
- [SD Card Modification](#sd-card-modification)
- [Video Guides](#video-guides)
- [More Videos](#more-videos)
- [Next Steps](#next-steps)

## Introduction
The Flipper Zero features a GPIO header on the top edge of it's body. This header is able to accommodate many different peripheral devices including ESP32 modules and development boards. Because of it's many available protocols, the Flipper Zero is able to communicate directly with the ESP32 Marauder firmware when it is properly installed on a compatible ESP32 expansion board. All that is requires is to install the ESP32 Marauder firmware on a compatible ESP32 development board, install the necessary application files on your Flipper Zero, connect your ESP32 development board to the Flipper Zero, and begin using the Marauder firmware features.

## Installation
The following documentation describes the steps necessary to install the ESP32 Marauder firmware on your ESP32 peripheral device as well as the applications necessary to prepare your Flipper Zero to interface with the Marauder firmware.

### Flipper Zero Preparation
Install anyone of the following third party firmwares on your Flipper Zero. They come prebuilt with the [WiFi Marauder](https://github.com/0xchocolate/flipperzero-firmware-with-wifi-marauder-companion) app created by [0xchocolate](https://github.com/0xchocolate). This application allows you to control the Marauder firmware from your Flipper Zero user interface. Simply find the WiFi Marauder application in your Flipper Zero application directory and execute it with your ESP32 expansion plugged into your Flipper Zero.
- [Momentum](https://momentum-fw.dev/update/) (**recommended**) 
- [Unleashed](https://github.com/DarkFlippers/unleashed-firmware)
- [Xtreme](https://github.com/ClaraCrazy/Flipper-Xtreme)
- [RogueMaster](https://github.com/RogueMaster/flipperzero-firmware-wPlugins)


### ESP32 Preparation

| Blue pill | Red Pill |
| --------- | -------- |
| Really easy. Nothing to change, but you don't get any customization (which is fine). | This pill is the size of a large pumpkin and it goes in your ass. Install libraries, build from source, know everything |
| [Spacehuhn Web Installer](https://github.com/justcallmekoko/ESP32Marauder/wiki/update-firmware#using-spacehuhn-web-updater), [FZ Marauder Flasher](https://github.com/UberGuidoZ/Flipper/tree/main/Wifi_DevBoard/FZ_Marauder_Flasher), or [FZEasyMarauderFlash](https://github.com/SkeletonMan03/FZEasyMarauderFlash) | [Build from source](installing-firmware-from-source)


## Having Issues?
If you are having issues with your Marauder installation either with the install process or with the firmware usage, be sure to check if your issue has already been solved in [FAQ](../faq). If not, feel free to join my [Discord](https://discord.gg/invite/w5JmasxvKA) to request help from the community or submit an [issue](https://github.com/justcallmekoko/ESP32Marauder/issues)

## SD Card Modification
**Note: We now offer a dedicated [SD adapter](https://www.justcallmekokollc.com/product/flipper-zero-wifi-dev-board-sd-expansion/7?cp=true&sa=true&sbp=false&q=false) and [SD/GPS adapter](https://www.justcallmekokollc.com/product/flipper-zero-dev-board-sd-gps/3?cp=true&sa=true&sbp=false&q=false) board for a clean install on the Flipper Zero WiFi Dev Board. No wires are necessary.**  
A MicroSD card can be attached to the Flipper Zero WiFi Dev Board SPI via a [MicroSD Breakout](https://www.sparkfun.com/products/544). Attaching a microSD card to the Flipper Zero WiFi Dev Board will allow the Marauder firmware to save captured WiFi traffic to storage in the form of PCAP files to be exported for analysis later. If you install this mod, **DO NOT** use the `flipper_sd_serial.bin` to update your WiFi Dev Board. Refer to the following table for the required solder connections

| MicroSD Breakout | Flipper Zero WiFi Dev Board |
| ---------------- | --------------------------- |
| VCC              | 3V3                         |
| GND              | GND                         |
| DI (MOSI)        | IO35                        |
| DO (MISO)        | IO37                        |
| SCK              | IO36                        |
| CS               | IO10                        |

Once all of the solder connections have been made, a piece of double-sided tape can be used to fix the MicroSD Breakout Board to the back of the WiFi Development Board.
<p align="left"><img alt="Marauder logo" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/IMG_5876%20-%20Copy.jpg?raw=true" width="300"><img alt="Marauder logo" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/IMG_5877%20-%20Copy.jpg?raw=true" width="300"></p><br>
<p align="left"><img alt="Marauder logo" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/IMG_5879%20-%20Copy.jpg?raw=true" width="500"></p>

## Video Guides
This video by [Lab 401](https://www.youtube.com/c/Lab401) will provide instructions for using the flasher script. This is the quickest way to get Marauder running on your device  
[![Tutorial](https://img.youtube.com/vi/um_acrDaBK4/0.jpg)](https://www.youtube.com/watch?v=um_acrDaBK4)

The following video describes and demonstrates the installation and usage of the Marauder firmware on the Flipper Zero WiFi Dev Board.  
[![Tutorial](https://img.youtube.com/vi/_YLTpNo5xa0/0.jpg)](https://www.youtube.com/watch?v=_YLTpNo5xa0)

## More videos
[![More Videos](https://img.youtube.com/vi/1ftcESq-pNY/0.jpg)](https://www.youtube.com/watch?v=1ftcESq-pNY)
[![More Videos](https://img.youtube.com/vi/nEBZ4VeTj7I/0.jpg)](https://www.youtube.com/watch?v=nEBZ4VeTj7I)
[![More Videos](https://img.youtube.com/vi/Deh5NBr0e_A/0.jpg)](https://www.youtube.com/watch?v=Deh5NBr0e_A)

## Next Steps
If you're trying to figure out what to do now that you have Marauder installed on your Flipper Zero, consider reading [Commandline](cli) for how to use the Marauder command line interface, and [Workflow Examples](workflow-examples) to understand how to use some of the commands in a practical way.