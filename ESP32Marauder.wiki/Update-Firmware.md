# Update Firmware

## Flipper Zero WiFi Dev Board, ESP32 Wemos D1 Mini Adapter
You can use any of the following options to update your Flipper Zero WiFi Dev Board Marauder firmware:
  - [SD Card/CLI Update](https://github.com/justcallmekoko/ESP32Marauder/wiki/update) **(recommended)**
  - [FZEE Flasher](https://fzeeflasher.github.io/) (**also recommended**)
  - [FZ Marauder Flasher](https://github.com/UberGuidoZ/Flipper/tree/main/Wifi_DevBoard/FZ_Marauder_Flasher)
  - [FZEasyMarauderFlash](https://github.com/SkeletonMan03/FZEasyMarauderFlash)
  - [Spacehuhn Web Updater](#using-spacehuhn-web-updater)

## ESP32 Marauder v4, v6, v7, Kit, Mini, WiFi Dev Board Pro:
You can use any of the following options to update your official ESP32 Marauder hardware:
  - [SD Card/CLI Update](https://github.com/justcallmekoko/ESP32Marauder/wiki/update) **(recommended)**
  - [FZEE Flasher](https://fzeeflasher.github.io/) (**also recommended**)
  - [Update Firmware Menu](update-firmware-menu)
  - [Spacehuhn Web Updater](#using-spacehuhn-web-updater)

## Using Spacehuhn Web Updater
You can use the web updater made by Spacehuhn to install/update the Marauder firmware on your device. To do so, you will need to provide the Marauder firmware file, partition file, and bootloader file to the installer. Please use the instructions and table below to update your device.

1. Go to the [Web Updater](https://esp.huhn.me/)
2. With your device plugged in, click connect
3. Use the following table to select the appropriate files and place them at the corresponding address
    - Enter the address shown as the blue text in the appropriate space and add the file linked to that blue text  

|            | ESP32 Marauder v4, v6, Kit, Mini, v7 | Flipper Zero WiFi Dev Board | Flipper Zero Multi Board S3 | WiFi Dev Board Pro/LDDB/NodeMCU-32S/ESP32 Wemos D1 Mini/BFFB |
| ---------- | -------------------------------- | --------------------------- | --------------------------- | ------------------ |
| Bootloader | [0x1000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/MarauderV4/esp32_marauder.ino.bootloader.bin) | [0x1000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroDevBoard/esp32_marauder.ino.bootloader.bin) | [0x0](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/esp32_marauder.ino.bootloader.bin) | [0x1000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/MarauderV4/esp32_marauder.ino.bootloader.bin) | 
| Partitions | [0x8000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/MarauderV4/esp32_marauder.ino.partitions.bin) | [0x8000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroDevBoard/esp32_marauder.ino.partitions.bin) | [0x8000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/esp32_marauder.ino.partitions.bin) | [0x8000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/MarauderV4/esp32_marauder.ino.partitions.bin) |
| Boot App   | [0xE000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/boot_app0.bin) | [0xE000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/boot_app0.bin) | [0xE000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/boot_app0.bin) | [0xE000](https://github.com/justcallmekoko/ESP32Marauder/raw/master/FlashFiles/FlipperZeroMultiBoardS3/boot_app0.bin) |
| Firmware   | [0x10000](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) | [0x10000](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) | [0x10000](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) | [0x10000](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) |

  - Use the following table to select the proper Marauder binary for your hardware. Please refer to version marking on your Marauder hardware is present.  

| Hardware | Binary Version |
| -------- | -------------- |
| v4 (OG) | `_old_hardware.bin` |
| v6 | `_new_hardware.bin`/`_v6.bin` |
| v6.1/v6.2 | `_v6_1.bin` |
| v7 | `_v7.bin` |
| Kit | `_kit.bin` |
| Mini | `_mini.bin` |
| Flipper Zero | `_flipper.bin` |
| MutliBoard S3 | `_multiboardS3.bin` |
| LDDB/NodeMCU/Wemos | `_lddb.bin` |
| Dev Board Pro | `_marauder_dev_board_pro.bin` |
| BFFB | `_marauder_dev_board_pro.bin` |
| ESP32-S2 Reverse Feather | `_rev_feather.bin` |
| CYD 2432S028(R) | `_cyd_2432S028.bin` |
| RL Phantom | `_cyd_2432S024_guition.bin` |
| CYD 2432S028 2 USB | `_cyd_2432S028_2usb.bin` |
| CYD 3.5inch | `_cyd_3_5_inch.bin` |
| M5 Cardputer | `_m5cardputer.bin` (Available on M5 Burner) |
| ESP32-C5 DevKit | [`_esp32c5_devkit.bin`](https://github.com/justcallmekoko/ESP32Marauder/wiki/ESP32%E2%80%90C5%E2%80%90DevKitC%E2%80%901) |
| AWOK V2/V3 screen (white usb) | `_v6_1.bin` |
| AWOK V2 flipper (orange usb) | `_flipper.bin` |
| AWOK V3 flipper (orange usb) | `_marauder_dev_board_pro.bin` |

4. Click program and wait. Once it says "To run the new firmware please reset your device", either hit the reset button or just flick the power switch off and back on