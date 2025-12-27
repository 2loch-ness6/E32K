#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/BinaryInterface.cpp"
#include "BinaryInterface.h"

extern WiFiScan wifi_scan_obj;
extern CommandLine cli_obj;

BinaryInterface::BinaryInterface() {
}

void BinaryInterface::main(uint32_t currentTime) {
    if (Serial.available()) {
        if (Serial.peek() == PROTOCOL_START_BYTE) {
            // It's a binary packet
            uint8_t start = Serial.read(); // Consume start
            // Wait for header (CMD + LEN)
            // Note: This is blocking for simplicity, but in a real loop we should state-machine it.
            // Given "near asynchronomic", a small block for header is acceptable if baud is high.
            
            // Timeout safety
            uint32_t timeout = millis();
            while (Serial.available() < 2 && millis() - timeout < 100) { delay(1); }
            if (Serial.available() < 2) return; // Timeout
            
            uint8_t cmd = Serial.read();
            uint8_t len = Serial.read();
            
            uint8_t payload[256];
            size_t readLen = 0;
            timeout = millis();
            while (readLen < len && millis() - timeout < 200) {
                if (Serial.available()) {
                    payload[readLen++] = Serial.read();
                }
            }
            
            // Optional CRC check could go here
            
            processPacket(cmd, payload, len);
        }
    }
}

void BinaryInterface::processPacket(uint8_t cmd, uint8_t* payload, size_t len) {
    // Map IDs to Actions
    switch (cmd) {
        case CMD_PING:
            sendResponse(RESP_PONG, NULL, 0);
            break;
            
        case CMD_SCAN_AP:
            // Mimic: "scanap"
            wifi_scan_obj.StartScan(WIFI_SCAN_TARGET_AP, TFT_MAGENTA);
            sendResponse(RESP_ACK, NULL, 0);
            break;
            
        case CMD_STOP_SCAN:
            wifi_scan_obj.StartScan(WIFI_SCAN_OFF);
            sendResponse(RESP_ACK, NULL, 0);
            break;
            
        case CMD_REBOOT:
            sendResponse(RESP_ACK, NULL, 0);
            ESP.restart();
            break;
            
        default:
            sendResponse(RESP_NACK, NULL, 0);
            break;
    }
}

void BinaryInterface::sendResponse(uint8_t type, uint8_t* data, size_t len) {
    Serial.write(PROTOCOL_START_BYTE);
    Serial.write(type);
    Serial.write((uint8_t)len);
    if (len > 0 && data != NULL) {
        Serial.write(data, len);
    }
    Serial.write(PROTOCOL_END_BYTE);
}
