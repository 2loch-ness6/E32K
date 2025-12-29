#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/BinaryInterface.h"
#ifndef BinaryInterface_h
#define BinaryInterface_h

#include "Arduino.h"
#include "MarauderProtocol.h"
#include "CommandLine.h"
#include "WiFiScan.h"

class BinaryInterface {
public:
    BinaryInterface();
    void main(uint32_t currentTime);
    
private:
    void processPacket(uint8_t cmd, uint8_t* payload, size_t len);
    void sendResponse(uint8_t type, uint8_t* data, size_t len);
    
    // Packet structure: [START][CMD][LEN][PAYLOAD...][CRC][END]
    // Minimal parser state
    bool reading = false;
};

#endif
