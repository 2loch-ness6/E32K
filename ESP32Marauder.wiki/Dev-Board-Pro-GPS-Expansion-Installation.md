# Dev Board Pro GPS Expansion Installation

The following documentation describes the installation process for the GPS Expansion of the Flipper Zero WiFi Dev Board Pro. Upon completion of the installation, all components should be contained within the same footprint as the original Dev Board Pro.

## Components
The following components are required to install the GPS Expansion. These components are already included in the kit available in our shop.

- 1x Flipper Zero Dev Board Pro
- 1x GPS Expansion Board
- 1x 18x18mm ceramic GPS antenna
- 1x Replacement face plate
- 4x M4x12mm machine screws
- 4x 30AWG wires

## Installation
1. Remove all of the screws from the Dev Board Pro including the button top screw on the underside of the enclosure
2. Remove the existing face plate from the enclosure
    - Save the light pipe located in the center of the face plate between the buttons
3. Remove the Dev Board Pro board from the enclosure
4. Prepare all four 30AWG wires by stripping both ends of each wire
5. Solder one end of each wire to their respective solder pads on the GPS expansion board labeled `3V3`, `GND`, `TX`, and `RX`
6. Use the following table to solder the other ends of the wires to the GPIO pins of the Dev Board Pro which are located on the edges of the PCB

| GPS Expansion | Dev Board Pro |
| ------------- | ------------- |
| `3V3`         | `3V3`         |
| `GND`         | `GND`         |
| `TX`          | `21`          |
| `RX`          | `17`          |

7. Twist the GPS Expansion board to twist the wires into a single column for easier storage
8. Install the GPS antenna into the replacement face plate with the back of the antenna facing away from the face plate
    - Use the channel coming from the antenna space in the face plate to route the pigtail cable to the other cavity of the face plate
9. Connect the small IPEX connector to the GPS Expansion Board
10. Install the Dev Board Pro board back into the existing body of the Dev Board Pro enclosure
11. Sandwich the GPS Expansion board between the Dev Board Pro enclosure body and the replacement face plate
12. Install the M2x12mm screws in each of the corners of the enclosure
13. Reinstall the button screw on the bottom of the Dev Board Pro enclosure
14. Install the saved light pipe into the light pipe hole on the replacement face plate which is located in the same spot as the original face plate
15. Ensure everything is installed correctly by opening the ESP32 Marauder companion app on your Flipper Zero with your Dev Board Pro plugged in, opening "View Log End", and pressing the physical reset button on the Dev Board Pro
    - Scroll to the top of the log and check for a message that says, "GPS Attached Successfully"

Good job, handsome!