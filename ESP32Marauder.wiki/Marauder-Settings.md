# Marauder Settings
The Marauder behavior can be slightly personalized to your preferences with the settings shown here. If you want to know how to change these settings, see [settings](settings-cmd) for the CLI and [Settings](settings) for the gui.  
The following table describes the settings available to the user.

| Name | Data Type | Default Value | Description |
| ---- | --------- | ------------- | ----------- |
| `ForcePMKID` | `bool` | `false`  | Send deauthentication frames when access points are detected during [`sniffpmkid`](sniffpmkid) |
| `ForceProbe` | `bool` | `false`  | Send deauthentication frames when access points are detected during [`sniffprobe`](sniffprobe) |
| `SavePCAP`   | `bool` | `true`   | Save captured WiFi data to PCAP files on an attached SD card |
| `EnableLED`  | `bool` | `true`   | Enable/Disable the status indicator LED included on specific hardware |
| `EPDeauth`   | `bool` | `false`  | Send deauthentication frames against any APs that are marked selected while executing Evil Portal |