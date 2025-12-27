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
    uint8_t byte;
    
    // In WAIT_START state, use peek() to avoid consuming non-binary data
    if (currentState == WAIT_START) {
      byte = Serial.peek();
    } else {
      byte = Serial.read();
    }
    
    switch (currentState) {
      case WAIT_START:
        if (byte == START_BYTE) {
          // Consume the start byte now that we know it's binary
          Serial.read();
          currentState = WAIT_CMD;
          payloadIndex = 0;
        } else {
          // Not a binary packet start - leave byte in buffer for CLI
          break;
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
        } else if (currentLen > sizeof(payloadBuffer)) {
          // Payload too large - will discard and NACK
          currentState = WAIT_PAYLOAD;
          payloadIndex = 0;
        } else {
          currentState = WAIT_PAYLOAD;
          payloadIndex = 0;
        }
        break;
        
      case WAIT_PAYLOAD:
        // Check if payload length exceeds buffer size
        if (currentLen > sizeof(payloadBuffer)) {
          // Discard oversized payload bytes without writing to buffer
          payloadIndex++;
          if (payloadIndex >= currentLen) {
            currentState = WAIT_END;
          }
        } else {
          // Normal case: safe to write into payload buffer
          if (payloadIndex < sizeof(payloadBuffer)) {
            payloadBuffer[payloadIndex++] = byte;
            if (payloadIndex >= currentLen) {
              currentState = WAIT_END;
            }
          } else {
            // Safety check: shouldn't reach here, but reset if we do
            currentState = WAIT_START;
            payloadIndex = 0;
          }
        }
        break;
        
      case WAIT_END:
        if (byte == END_BYTE) {
          // Check if packet was oversized
          if (currentLen > sizeof(payloadBuffer)) {
            // Oversized packet: NACK it
            sendResponse(RESP_NACK, NULL, 0);
          } else {
            // Valid packet received
            handlePacket(currentCmd, payloadBuffer, currentLen);
          }
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
      // PING expects no payload
      if (len == 0) {
        sendResponse(RESP_PONG, NULL, 0);
      } else {
        sendResponse(RESP_NACK, NULL, 0);
      }
      break;

    case CMD_SCAN_AP:
      wifi_scan_obj.StartScan(WIFI_SCAN_AP);
      sendResponse(RESP_ACK, NULL, 0);
      break;

    case CMD_SCAN_STA:
      wifi_scan_obj.StartScan(WIFI_SCAN_STATION);
      sendResponse(RESP_ACK, NULL, 0);
      break;

    case CMD_STOP_SCAN:
      wifi_scan_obj.StopScan(wifi_scan_obj.currentScanMode);
      sendResponse(RESP_ACK, NULL, 0);
      break;

    case CMD_ATTACK:
        if (len > 0) {
            uint8_t attackType = payload[0];
            uint8_t attackMode = WIFI_SCAN_OFF;
            
            // Map simple attack IDs to Internal Constants
            if (attackType == 0x01) attackMode = WIFI_ATTACK_DEAUTH;
            else if (attackType == 0x02) attackMode = WIFI_ATTACK_BEACON_SPAM;
            else if (attackType == 0x03) attackMode = WIFI_ATTACK_RICK_ROLL;
            
            if (attackMode != WIFI_SCAN_OFF) {
                wifi_scan_obj.StartScan(attackMode);
                sendResponse(RESP_ACK, NULL, 0);
            } else {
                sendResponse(RESP_NACK, NULL, 0);
            }
        } else {
            sendResponse(RESP_NACK, NULL, 0);
        }
        break;

    case CMD_REBOOT:
        sendResponse(RESP_ACK, NULL, 0);
        delay(100);
        ESP.restart();
        break;

    case CMD_GENERIC_REQ:
        if (len > 0) {
            wifi_scan_obj.StartScan(payload[0]);
            sendResponse(RESP_ACK, NULL, 0);
        } else {
            sendResponse(RESP_NACK, NULL, 0);
        }
        break;
      
    case CMD_UPDATE_START:
      // UPDATE_START expects no payload
      if (len != 0) {
        sendResponse(RESP_NACK, NULL, 0);
        break;
      }
      // Try to begin update; only enter update mode on success
      if (Update.begin(UPDATE_SIZE_UNKNOWN)) {
        update_mode = true;
        sendResponse(RESP_ACK, NULL, 0);
      } else {
        update_mode = false;
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
