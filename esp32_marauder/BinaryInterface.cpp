#include "BinaryInterface.h"
#include "WiFiScan.h"

extern WiFiScan wifi_scan_obj;

BinaryInterface::BinaryInterface() {
  currentState = WAIT_START;
  currentCmd = 0;
  currentLen = 0;
  payloadIndex = 0;
  update_mode = false;
  matchIndex = 0;
}

bool BinaryInterface::isUpdateMode() {
  return update_mode;
}

bool BinaryInterface::isPending() {
  return currentState != WAIT_START;
}

void BinaryInterface::main(uint32_t currentTime) {
  // State machine for binary protocol parsing
  while (Serial.available() > 0) {
    uint8_t byte = Serial.read();
    
    switch (currentState) {
      case WAIT_START:
        if (byte == START_BYTE) {
          currentState = WAIT_CMD;
          payloadIndex = 0;
        }
        break;
        
      case WAIT_CMD:
        currentCmd = byte;
        currentState = WAIT_LEN;
        break;
        
      case WAIT_LEN:
        currentLen = byte;
        if (currentLen == 0) {
          currentState = WAIT_END;
        } else {
          currentState = WAIT_PAYLOAD;
          payloadIndex = 0;
        }
        break;
        
      case WAIT_PAYLOAD:
        payloadBuffer[payloadIndex++] = byte;
        if (payloadIndex >= currentLen) {
          currentState = WAIT_END;
        }
        break;
        
      case WAIT_END:
        if (byte == END_BYTE) {
          // Valid packet received
          handlePacket(currentCmd, payloadBuffer, currentLen);
        }
        // Reset state machine
        currentState = WAIT_START;
        payloadIndex = 0;
        break;
    }
  }
}

void BinaryInterface::handlePacket(uint8_t cmd, uint8_t* payload, uint8_t len) {
  switch (cmd) {
    case CMD_PING:
      sendResponse(RESP_PONG, NULL, 0);
      break;
      
    case CMD_UPDATE_START:
      // Enter update mode
      update_mode = true;
      if (Update.begin(UPDATE_SIZE_UNKNOWN)) {
        sendResponse(RESP_ACK, NULL, 0);
      } else {
        sendResponse(RESP_NACK, NULL, 0);
      }
      break;
      
    default:
      sendResponse(RESP_NACK, NULL, 0);
      break;
  }
}

void BinaryInterface::sendResponse(uint8_t cmd, uint8_t* payload, uint8_t len) {
  Serial.write(START_BYTE);
  Serial.write(cmd);
  Serial.write(len);
  if (len > 0 && payload != NULL) {
    Serial.write(payload, len);
  }
  Serial.write(END_BYTE);
}
