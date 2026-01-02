# SD Update
<p align="left">
  <img alt="ESP32 WROOM-32U" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/icons/sd_update_22.bmp?raw=true" width="100">
</p>

1. Download the [latest release](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) of the Marauder firmware
2. Copy the bin file you downloaded to the root of an SD card
3. Rename the bin file on the SD card to `update.bin`
4. With Marauder powered off, insert the SD card into Marauder
5. Power Marauder on and navigate to `Device`>`Update Firmware`>`SD Update`
    - `update -s` if you are using CLI/Flipper Zero
6. Click `Yes` to confirm the update
    - Marauder will automatically reboot once the update has been applied