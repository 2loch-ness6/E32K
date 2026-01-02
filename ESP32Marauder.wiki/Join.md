# Join WiFi
Allows the user to join their device to an existing WiFi network for further network operations

## Menu Path
`WiFi`>`General`>`Join WiFi`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage.

## Usage
```join -a <ap_index> -p <password>```

#### Arguments
| Argument | Required/Optional | Description |
| -------- | ----------------- | ----------- |
| `-a` | Required | Specify the index of the access point you want to join |
| `-p` | Required | The password of the access point |