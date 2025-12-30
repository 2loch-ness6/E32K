/*
 * Gemini-Link: High-Performance Binary Serial-to-Radio Bridge
 * 
 * Architecture:
 * - Host (Android/Linux) sends binary commands to control Radio.
 * - ESP32 sends binary events (captured packets) to Host.
 * - Zero internal logic. All intelligence lives on the Host.
 */

#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "driver/uart.h"
#include "esp_wifi.h"
#include "esp_event.h"
#include "esp_system.h"
#include "nvs_flash.h"
#include "esp_log.h"
#include "esp_bt.h"
#include "esp_gap_ble_api.h"
#include "esp_bt_main.h"

// --- Configuration ---
#define UART_PORT           UART_NUM_0
#define UART_BAUD_RATE      921600
#define RX_BUF_SIZE         4096
#define TX_BUF_SIZE         4096
#define PROTOCOL_START_BYTE 0xA5

// --- Protocol Commands (Host -> ESP) ---
enum {
    CMD_PING            = 0x01,
    CMD_SET_WIFI_CONFIG = 0x02, // [Channel: 1]
    CMD_TX_WIFI         = 0x03, // [Len: 2][RawFrame: N]
    CMD_SET_SNIFFER     = 0x04, // [Enable: 1]
    CMD_BT_SCAN_CTRL    = 0x05, // [Enable: 1]
    CMD_BT_TX           = 0x06  // [Len: 2][RawAdv: N]
};

// --- Protocol Events (ESP -> Host) ---
enum {
    EVT_ACK             = 0xA0, // [CmdId: 1][Status: 1]
    EVT_WIFI_RX         = 0xA1, // [Rssi: 1][Len: 2][Frame: N]
    EVT_BT_FOUND        = 0xA2, // [Mac: 6][Rssi: 1][AdvLen: 1][AdvData: N]
    EVT_LOG             = 0xAE  // [Len: 1][Text: N]
};

// --- Raw 802.11 TX Declaration ---
esp_err_t esp_wifi_80211_tx(wifi_interface_t ifx, const void *buffer, int len, bool en_sys_seq);

static const char *TAG = "GEMINI_LINK";
static QueueHandle_t uart_queue;

// --- Helper: Send Packet to Host ---
// Frame: [START][TYPE][LEN_L][LEN_H][PAYLOAD...][CRC]
// CRC is simple XOR of Payload
void send_packet_to_host(uint8_t type, const uint8_t *data, uint16_t len) {
    uint8_t header[4];
    header[0] = PROTOCOL_START_BYTE;
    header[1] = type;
    header[2] = len & 0xFF;
    header[3] = (len >> 8) & 0xFF;

    uart_write_bytes(UART_PORT, (const char*)header, 4);
    if (len > 0 && data != NULL) {
        uart_write_bytes(UART_PORT, (const char*)data, len);
    }
    
    // Simple XOR Checksum (Optional, keep lightweight)
    uint8_t crc = 0;
    for (int i=0; i<len; i++) crc ^= data[i];
    uart_write_bytes(UART_PORT, (const char*)&crc, 1);
}

void send_ack(uint8_t cmd_id, uint8_t status) {
    uint8_t data[] = {cmd_id, status};
    send_packet_to_host(EVT_ACK, data, 2);
}

// --- RX Callbacks ---

// Wi-Fi Promiscuous Callback
void wifi_sniffer_cb(void *buf, wifi_promiscuous_pkt_type_t type) {
    wifi_promiscuous_pkt_t *pkt = (wifi_promiscuous_pkt_t *)buf;
    // Prepare metadata + frame
    // We strictly minimize processing here.
    // Metadata: RSSI (1 byte)
    // Payload: Packet
    
    int total_len = pkt->rx_ctrl.sig_len + 1;
    if (total_len > 2000) return; // Safety

    // We allocate a small temp buffer on stack to merge RSSI + Frame for single UART write
    // Or just write sequentially to UART fifo (safer for stack)
    
    // Header
    uint8_t header[4];
    header[0] = PROTOCOL_START_BYTE;
    header[1] = EVT_WIFI_RX;
    header[2] = total_len & 0xFF;
    header[3] = (total_len >> 8) & 0xFF;
    uart_write_bytes(UART_PORT, (const char*)header, 4);

    // Metadata
    int8_t rssi = pkt->rx_ctrl.rssi;
    uart_write_bytes(UART_PORT, (const char*)&rssi, 1);

    // Frame
    uart_write_bytes(UART_PORT, (const char*)pkt->payload, pkt->rx_ctrl.sig_len);

    // CRC (XOR of RSSI + Frame)
    uint8_t crc = (uint8_t)rssi;
    for (int i=0; i<pkt->rx_ctrl.sig_len; i++) crc ^= pkt->payload[i];
    uart_write_bytes(UART_PORT, (const char*)&crc, 1);
}

// BLE Scan Callback
static void esp_gap_cb(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param) {
    if (event == ESP_GAP_BLE_SCAN_RESULT_EVT) {
        if (param->scan_rst.search_evt == ESP_GAP_SEARCH_INQ_RES_EVT) {
            // Found device
            // Struct: [Mac: 6][Rssi: 1][AdvLen: 1][AdvData: N]
            uint16_t adv_len = param->scan_rst.adv_data_len;
            if (adv_len > 60) adv_len = 60; // Cap to keep simple

            uint8_t payload[70];
            memcpy(payload, param->scan_rst.bda, 6);
            payload[6] = (uint8_t)param->scan_rst.rssi;
            payload[7] = (uint8_t)adv_len;
            memcpy(&payload[8], param->scan_rst.ble_adv, adv_len);

            send_packet_to_host(EVT_BT_FOUND, payload, 8 + adv_len);
        }
    }
}

// --- Command Processor ---

void process_command(uint8_t cmd, uint8_t *data, uint16_t len) {
    switch (cmd) {
        case CMD_PING:
            send_ack(CMD_PING, 0x00); // OK
            break;

        case CMD_SET_WIFI_CONFIG:
            if (len >= 1) {
                uint8_t channel = data[0];
                esp_wifi_set_channel(channel, WIFI_SECOND_CHAN_NONE);
                send_ack(CMD_SET_WIFI_CONFIG, 0x00);
            }
            break;

        case CMD_TX_WIFI:
            // Data is raw 802.11 frame
            esp_wifi_80211_tx(WIFI_IF_AP, data, len, false);
            // No ACK for TX to keep speed up, host assumes success
            break;

        case CMD_SET_SNIFFER:
            if (len >= 1) {
                bool enable = data[0];
                if (enable) {
                    esp_wifi_set_promiscuous_rx_cb(&wifi_sniffer_cb);
                    esp_wifi_set_promiscuous(true);
                } else {
                    esp_wifi_set_promiscuous(false);
                }
                send_ack(CMD_SET_SNIFFER, 0x00);
            }
            break;
            
        case CMD_BT_SCAN_CTRL:
            if (len >= 1) {
                bool enable = data[0];
                if (enable) {
                    static esp_ble_scan_params_t scan_params = {
                        .scan_type = BLE_SCAN_TYPE_PASSIVE,
                        .own_addr_type = BLE_ADDR_TYPE_PUBLIC,
                        .scan_filter_policy = BLE_SCAN_FILTER_ALLOW_ALL,
                        .scan_interval = 0x50,
                        .scan_window = 0x30,
                        .scan_duplicate = BLE_SCAN_DUPLICATE_DISABLE
                    };
                    esp_ble_gap_set_scan_params(&scan_params);
                    esp_ble_gap_start_scanning(0);
                } else {
                    esp_ble_gap_stop_scanning();
                }
                send_ack(CMD_BT_SCAN_CTRL, 0x00);
            }
            break;

        default:
            send_ack(cmd, 0xFF); // Unknown
            break;
    }
}

// --- Serial Protocol Parser Task ---
void protocol_task(void *pvParam) {
    uart_event_t event;
    uint8_t *dtmp = (uint8_t *) malloc(RX_BUF_SIZE);
    
    // Parser State
    int state = 0; // 0=WaitStart, 1=Cmd, 2=LenL, 3=LenH, 4=Data, 5=CRC
    uint8_t cmd = 0;
    uint16_t len = 0;
    uint16_t idx = 0;
    uint8_t parser_crc = 0;
    uint8_t *payload_buf = (uint8_t *) malloc(2048); // Max payload

    for (;;) {
        if (xQueueReceive(uart_queue, (void * )&event, (TickType_t)portMAX_DELAY)) {
            if (event.type == UART_DATA) {
                int read_len = uart_read_bytes(UART_PORT, dtmp, event.size, portMAX_DELAY);
                
                for (int i = 0; i < read_len; i++) {
                    uint8_t byte = dtmp[i];
                    
                    switch (state) {
                        case 0: // Start Byte
                            if (byte == PROTOCOL_START_BYTE) state = 1;
                            break;
                        case 1: // Command
                            cmd = byte;
                            state = 2;
                            break;
                        case 2: // Len L
                            len = byte;
                            state = 3;
                            break;
                        case 3: // Len H
                            len |= (byte << 8);
                            if (len > 2000) { state = 0; } // Invalid size
                            else if (len == 0) { state = 5; } // No payload, skip to CRC
                            else { state = 4; idx = 0; parser_crc = 0; }
                            break;
                        case 4: // Data
                            payload_buf[idx++] = byte;
                            parser_crc ^= byte;
                            if (idx >= len) state = 5;
                            break;
                        case 5: // CRC
                            // if (byte == parser_crc) { // Enable CRC check if desired
                                process_command(cmd, payload_buf, len);
                            // }
                            state = 0;
                            break;
                    }
                }
            } else if (event.type == UART_BUFFER_FULL) {
                uart_flush_input(UART_PORT);
                xQueueReset(uart_queue);
            }
        }
    }
    free(dtmp);
    free(payload_buf);
    vTaskDelete(NULL);
}

void app_main(void) {
    // 1. Init NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    // 2. Setup UART (High Speed)
    uart_config_t uart_config = {
        .baud_rate = UART_BAUD_RATE,
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_DEFAULT,
    };
    // Install UART driver with Event Queue
    ESP_ERROR_CHECK(uart_driver_install(UART_PORT, RX_BUF_SIZE * 2, TX_BUF_SIZE * 2, 20, &uart_queue, 0));
    ESP_ERROR_CHECK(uart_param_config(UART_PORT, &uart_config));
    ESP_ERROR_CHECK(uart_set_pin(UART_PORT, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE));

    // 3. Setup Wi-Fi (Promiscuous capable)
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));
    ESP_ERROR_CHECK(esp_wifi_set_storage(WIFI_STORAGE_RAM));
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP)); // AP mode allows setting specific channels + TX
    ESP_ERROR_CHECK(esp_wifi_start());
    ESP_ERROR_CHECK(esp_wifi_set_ps(WIFI_PS_NONE)); // No power save for max perf

    // 4. Setup Bluetooth (BLE Only for now to save RAM/Complexity, can add Classic later)
    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    ret = esp_bt_controller_init(&bt_cfg);
    if (ret == ESP_OK) {
        esp_bt_controller_enable(ESP_BT_MODE_BLE);
        esp_bluedroid_init();
        esp_bluedroid_enable();
        esp_ble_gap_register_callback(esp_gap_cb);
    }

    // 5. Start Protocol Task
    xTaskCreate(protocol_task, "gemini_link", 4096, NULL, 10, NULL);
    
    // Notify Host we are alive
    const char *boot_msg = "Gemini-Link Ready";
    send_packet_to_host(EVT_LOG, (uint8_t*)boot_msg, strlen(boot_msg));
}
