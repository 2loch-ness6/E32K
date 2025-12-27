#line 1 "/root/E32K/MarauderController/app/src/main/assets/esp32_marauder/MarauderProtocol.h"
#ifndef MarauderProtocol_h
#define MarauderProtocol_h

#define PROTOCOL_START_BYTE 0xA5
#define PROTOCOL_END_BYTE   0x5A

// Command IDs
#define CMD_PING            0x00
#define CMD_SCAN_AP         0x01
#define CMD_SCAN_STA        0x02
#define CMD_STOP_SCAN       0x03
#define CMD_ATTACK          0x04
#define CMD_GET_CONFIG      0x05
#define CMD_REBOOT          0x06
#define CMD_UPDATE          0x07

// Response IDs
#define RESP_ACK            0x00
#define RESP_NACK           0x01
#define RESP_PONG           0x02

#endif
