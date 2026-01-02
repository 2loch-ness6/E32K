# Bad Msg
This frame spoofs Message 1 of the 4‑Way Handshake with the Install bit set and an all‑zero MIC.
Because the Install flag is illegal in Msg 1, most wpa_supplicant/IWD, Windows, macOS / iOS and Android clients treat the frame as corrupted, immediately abort the handshake and drop the connection. The attack method is used to bypass frame protection implemented in WiFi 6. When executing the attack, you can select between attacking all stations associated with an access point, or only specific stations. Frames crafted during this attack are addressed to specific MACs, not broadcast, even during a full coverage attack. More information on how the attack functions can be found [here](https://papers.mathyvanhoef.com/wisec2022.pdf).

If you chose the full coverage attack, all stations associated with APs marked as selected will be attacked. If you choose "targeted", only stations marked as selected will be attacked, regardless of which APs are marked as selected.

## CLI Usage
`attack -t badmsg [-c]`

| Argument | Required/Optional | Description |
| -------- | ----------------- | ----------- |
| `-c` | Optional | Only attack stations marked as selected|