# Flock Sniff
Flock Sniff uses a combination of Bluetooth and WiFi sniffing methods to detect misconfigured or legacy Flock Safety cameras. Certain models of Flock cameras have remote batteries which relay battery health information over Bluetooth to the camera itself. This Bluetooth traffic, once detected, presents a high likelihood the device is indeed a Flock camera. Additionally Flock Safety devices which are misconfigured or in a troubleshooting state emit probe requests in search of admin devices or beacon frames to allow Flock admins to configure them over the air. This traffic can also be detected. For additional information on Flock Safety cameras and their vulnerabilities, check [this blog](https://gainsec.com/2025/09/27/button-presses-to-shell-on-flock-safety-license-plate-cameras-over-wi-fi/). For other information regarding detection, check this blog post [here](https://www.ryanohoro.com/post/spotting-flock-safety-s-falcon-cameras).

## Menu Path
`Bluetooth`>`Sniffers`>`Flock Sniff`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage. The Airtag Sniff function can be stopped with [stopscan](https://github.com/justcallmekoko/ESP32Marauder/wiki/stopscan).

`sniffbt -t flock`