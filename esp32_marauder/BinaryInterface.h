#ifndef BinaryInterface_h
#define BinaryInterface_h

#include <Arduino.h>
#include <Update.h>
#include "configs.h"

// Protocol Constants
#define START_BYTE 0xA5
#define END_BYTE 0x5A

#define CMD_PING 0x00
#define CMD_SCAN_AP 0x01
#define CMD_SCAN_STA 0x02
#define CMD_STOP_SCAN 0x03
#define CMD_ATTACK 0x04
#define CMD_GET_CONFIG 0x05
#define CMD_REBOOT 0x06
#define CMD_UPDATE_START 0x07
#define CMD_FS_LIST 0x08
#define CMD_FS_DELETE 0x09
#define CMD_FS_READ 0x0A
#define CMD_GENERIC_REQ 0x10

#define RESP_ACK 0x00
#define RESP_NACK 0x01
#define RESP_PONG 0x02
#define RESP_SCAN_DATA 0x20

class BinaryInterface {
  public:
    BinaryInterface();
    void main(uint32_t currentTime);
    bool isUpdateMode();
    bool isPending();
    void sendResponse(uint8_t cmd, uint8_t* payload, uint8_t len);

  private:
    void handlePacket(uint8_t cmd, uint8_t* payload, uint8_t len);
    // void sendResponse(uint8_t cmd, uint8_t* payload, uint8_t len); // Moved to public
    
    // Protocol Constants (Moved to global #defines)

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
