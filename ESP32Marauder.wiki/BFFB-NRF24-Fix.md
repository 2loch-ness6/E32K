# BFFB NRF24 Fix
There is a known issue affecting some BFFB devices which causes the NRF24 Sniffer application on the Flipper Zero to crash and freeze the Flipper Zero. During our testing, we found that if you have an NRF24 module as well as an ESP32 module connected to the Flipper Zero and powered, it causes a communication conflict while using that specific Flipper App. This issue does not seem to affect other apps like the NRF channel scanner for example. We have since begun developing a hardware revision for the BFFB which compensates for this issue. For the hardware revisions which do not have the fix implemented in the design, there is a simple hardware patch you can do which will achieve the same result. Essentially the goal is to remove power from the ESP32 module when the NRF24 module is powered and vice versa. 

## Modification
1. Using a razor blade, sever the PCB trace connection between the ESP32 3V3 pin and the 3V3 rail
<img src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/BFFB%20NRF24%20Fix/DSC05049-Edit.jpg?raw=true" alt="3V3 Trace" height="500">

2. Solder a wire connecting the 3V3 pin of the ESP32 module to the top left pin of the CC1101/NRF24 switch
<img src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/BFFB%20NRF24%20Fix/DSC05050.jpg?raw=true" alt="Wire Patch" height="500">

## Conclusion
Perform a function test to ensure when the NRF24 is powered, you can successfully perform a sniffing action using the NRF24 Sniffer application on your Flipper Zero. When switching to the ESP32 module to use the Marauder companion app, for example, you must place the switch in the "CC1101" position. You might find it necessary to preset the reset button on the BFFB to reset the ESP32 module.
