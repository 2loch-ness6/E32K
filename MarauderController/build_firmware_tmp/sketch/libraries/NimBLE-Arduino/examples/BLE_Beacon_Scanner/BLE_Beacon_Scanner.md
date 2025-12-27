#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/libraries/NimBLE-Arduino/examples/BLE_Beacon_Scanner/BLE_Beacon_Scanner.md"
## BLE Beacon Scanner

Initiates a BLE device scan.
Checks if the discovered devices are 
- an iBeacon
- an Eddystone TLM beacon
- an Eddystone URL beacon

and sends the decoded beacon information over Serial log