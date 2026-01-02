# Association Sleep Attack
In an association sleep attack, an attacker forges an Association Request frame that has the power-management “sleep” bit set and uses the victim’s MAC address. Because the frame is unauthenticated, a 802.11w-protected AP should reject it, yet, thinking the client is asleep, it buffers the follow-up SA-Query “are-you-alive?” messages instead of sending them. When the SA-Query timer expires the AP assumes the client is gone and tears down the protected session, silently kicking the real user off the network. In this way a single spoofed, plaintext frame bypasses Management Frame Protection and performs a denial-of-service against the victim. More information on how the attack functions can be found [here](https://papers.mathyvanhoef.com/wisec2022.pdf).

If you chose the full coverage attack, all stations associated with APs marked as selected will be attacked. If you choose "targeted", only stations marked as selected will be attacked, regardless of which APs are marked as selected.

## CLI Usage
`attack -t sleep [-c]`

| Argument | Required/Optional | Description |
| -------- | ----------------- | ----------- |
| `-c` | Optional | Only attack stations marked as selected|