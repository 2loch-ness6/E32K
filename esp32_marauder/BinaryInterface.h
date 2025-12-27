#ifndef BinaryInterface_h
#define BinaryInterface_h

#include <Arduino.h>
#include <Update.h>
#include "configs.h"

class BinaryInterface {
  public:
    BinaryInterface();
    void main(uint32_t currentTime);
    bool isUpdateMode();
    bool isPending();
    
  private:
    void handlePacket(uint8_t cmd, uint8_t* payload, uint8_t len);
    void sendResponse(uint8_t cmd, uint8_t* payload, uint8_t len);
    
    // Protocol Constants
    const uint8_t START_BYTE = 0xA5;
    const uint8_t END_BYTE = 0x5A;
    
    const uint8_t CMD_PING = 0x00;
    const uint8_t CMD_SCAN_AP = 0x01;
    const uint8_t CMD_SCAN_STA = 0x02;
    const uint8_t CMD_STOP_SCAN = 0x03;
    const uint8_t CMD_ATTACK = 0x04;
    const uint8_t CMD_GET_CONFIG = 0x05;
    const uint8_t CMD_REBOOT = 0x06;
    const uint8_t CMD_UPDATE_START = 0x07;
    const uint8_t CMD_GENERIC_REQ = 0x10;
    
    const uint8_t RESP_ACK = 0x00;
    const uint8_t RESP_NACK = 0x01;
    const uint8_t RESP_PONG = 0x02;

    // State machine
    enum State {
        WAIT_START,
        WAIT_CMD,
        WAIT_LEN,
        WAIT_PAYLOAD,
        WAIT_END
    };

    State currentState = WAIT_START;
    uint8_t currentCmd = 0;
    uint8_t currentLen = 0;
    uint8_t payloadBuffer[256];
    uint8_t payloadIndex = 0;

    bool update_mode = false;
    
    // Update matching
    const char* UPDATE_END_CMD = "#update complete";
    uint8_t matchIndex = 0;
};

#endif
