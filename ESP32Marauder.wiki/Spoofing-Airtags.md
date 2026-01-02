# Spoofing Airtags
The following documentation describes the steps necessary for spoofing Airtag data in Bluetooth Low Energy advertisements

1. Sniff nearby Airtags to store their advertisement data
    - `sniffat`

2. Display list of sniffed Airtags to determine target index
    - `list -t`

3. Execute Airtag spoof using the target index shown in the previous list
    - `spoofat -t <index>`