# Web Update
<p align="left">
  <img alt="ESP32 WROOM-32U" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/icons/web_update_22.bmp?raw=true" width="100">
</p>

1. Download the [latest release](https://github.com/justcallmekoko/ESP32Marauder/releases/latest) of the Marauder firmware

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

2. With Marauder powered on, navigate to `Device`>`Update Firmware`>`Web Update`
    - `update -w` if you are using CLI
    - Marauder will display details on screen about the status of the update
3. Connect to the MarauderOTA WiFi network from your computer
    - password: justcallmekoko
4. On your web browser, navigate to `http://192.168.4.1`
5. Enter the username and password
    - Username: admin
    - Password: admin
6. Click the `Browse` button and select the .bin file you downloaded from the releases
7. Click `Update`
    - Marauder will automatically reboot once the update has been applied