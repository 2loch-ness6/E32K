# update
Performs and firmware update of the ESP32 using a connected SD card. You must have an SD card connected to the ESP32 SPI interface and be using a version of the firmware which expects an SD card.

## Preparation
Follow the instructions under [SD Update](https://github.com/justcallmekoko/ESP32Marauder/wiki/SD-Update)

## CLI/Flipper Zero
Ensure you follow the directions under [SD Update](https://github.com/justcallmekoko/ESP32Marauder/wiki/sd-update) prior to using the following CLI/Flipper Zero command to update your Marauder firmware. With the Wifi Marauder application open on your Flipper Zero, locate the `Update` option and select. If you followed the SD Update instructions properly, you will see a message indicating the firmware installation process has started followed shortly after by a reboot message.

### Usage
```update -s/```

#### Arguments
| Argument | Required/Optional | Description |
| -------- | ----------------- | ----------- |
| `-s/` | Required | Update your firmware using SD card |

- `-s`: [SD Update](https://github.com/justcallmekoko/ESP32Marauder/wiki/sd-update)