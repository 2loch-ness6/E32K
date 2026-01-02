# Battery Fuel Gauge
As of Marauder firmware v1.2.0, a battery fuel gauge can be added to any ESP32 Marauder that features a screen with full UI and accessible GPIO for I2C connections. The battery fuel gauge is a firmware feature as well as a hardware feature and requires physical modification for any Marauder hardware which does not already come with the necessary components. If your Marauder does not display a battery percentage in the status bar, you will be required to perform this modification in order to access this feature.

### Required Materials
- [Adafruit MAX17048](https://www.adafruit.com/product/5580)
- Assorted wire
- Double sided adhesive tape
- Kapton tape

### Installation
1. Ensure your Marauder hardware is powered off and the battery uninstalled
2. Shield the entire backside of the the MAX17048 with kapton tape
3. Using double sided adhesive tape, stick the MAX17048 to an available spot within your Marauder enclosure
    - Using the v6 for example, stick the MAX1708 to the metal shield of the ESP32 module
4. Use the following connection table to connect power and battery between the MAX17048 and your Marauder
    - You will need to locate the connections on your PCB as they are likely not clearly marked  

| MAX17048 | ESP32 Marauder      |
| -------- | ------------------- |
| `VIN`    | `3V3`               |
| `GND`    | `GND`               |
| `+`      | `+` of your battery |
| `-`      | `-` of your battery (I mean why not) |

5. Use the following connection table to make I2C connections between MAX17048 and your Marauder

| Model | SDA | SCL |
| ----- | --- | --- |
| v4    | GPIO33  | GPIO22  |
| v6    | GPIO33  | GPIO22  |
| v6.1  | GPIO33  | GPIO22  |
| v7    | GPIO33  | GPIO16  |
| Kit   | GPIO33  | GPIO22  |
| Mini  | GPIO33  | GPIO26  |
| StickC| GPIO33  | GPIO22  |

6. Reinstall your battery and enclosure
7. Power your Marauder on and verify you see a battery percentage appear on your status bar
    - It may display as `0%` at first and update to the correct percentage after a couple of seconds