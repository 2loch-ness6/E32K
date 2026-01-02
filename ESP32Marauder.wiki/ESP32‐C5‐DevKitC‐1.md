## Install Firmware
1. Clone this repo
2. In your workstation CLI, navigate to the `C5_Py_Flasher` directory
3. With your ESP32-C5 device unplugged, execute `python c5_flasher.py` and allow any missing python packages to install
4. Once you see `Waiting for ESP32-C5 device to be connected...`, connect your ESP32-C5 device to your PC via USB-C cable
5. Once you see `Ready to flash these files to ESP32-C5? (y/N):`, enter `y` and allow the firmware to flash
6. When the `Hardware reset` message appears on the screen, you may disconnect your ESP32-C5 device

### 3V3 Jumper
- For the C5 Flipper Zero Adapter, do not remove the 3V3 jumper

### [Installation Video](https://www.youtube.com/watch?v=rVNPAnEKGd4)

## Next steps

### GPS Connections
- Check [GPS Modification](gps-modification)

### SD Card Connections
| SD Card | ESP32-C5 |
| ------- | -------- |
| MISO    | GPIO2    |
| MOSI    | GPIO7    |
| SCK     | GPIO6    |
| CS      | GPIO10   |

