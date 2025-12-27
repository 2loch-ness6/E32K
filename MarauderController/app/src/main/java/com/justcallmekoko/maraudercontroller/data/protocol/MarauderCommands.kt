package com.justcallmekoko.maraudercontroller.data.protocol

/**
 * ESP32 Marauder command definitions based on CommandLine.cpp
 */
object MarauderCommands {
    
    // Admin Commands
    const val HELP = "help"
    const val REBOOT = "reboot"
    const val UPDATE = "update"
    const val SETTINGS = "settings"
    const val CHANNEL = "channel"
    const val LED = "led"
    const val LS = "ls"
    
    // WiFi Scanning
    const val SCAN_AP = "scanap"
    const val SCAN_STA = "scansta"
    const val SCAN_ALL = "scanall"
    const val SNIFF_BEACON = "sniffbeacon"
    const val SNIFF_PROBE = "sniffprobe"
    const val SNIFF_PWN = "sniffpwn"
    const val SNIFF_ESP = "sniffesp"
    const val SNIFF_DEAUTH = "sniffdeauth"
    const val SNIFF_PMKID = "sniffpmkid"
    const val SNIFF_RAW = "sniffraw"
    const val SNIFF_MULTISSID = "sniffmultissid"
    const val SNIFF_PINESCAN = "pinescan"
    const val SIGSTREN = "sigstren"
    const val WARDRIVE = "wardrive"
    const val STOP_SCAN = "stopscan"
    
    // WiFi Attacks
    const val ATTACK = "attack"
    const val ATTACK_DEAUTH = "deauth"
    const val ATTACK_BEACON = "beacon"
    const val ATTACK_PROBE = "probe"
    const val ATTACK_RickRoll = "rickroll"
    const val ATTACK_MIMIC = "mimic"
    const val EVIL_PORTAL = "evilportal"
    const val KARMA = "karma"
    
    // Network
    const val PING = "ping"
    const val ARP_SCAN = "arpscan"
    const val PORT_SCAN = "portscan"
    const val PACKET_COUNT = "packetcount"
    
    // WiFi Aux
    const val LIST_AP = "list"
    const val SELECT = "select"
    const val SSID = "ssid"
    const val CLEAR_AP = "clearap"
    const val SAVE = "save"
    const val LOAD = "load"
    const val JOIN = "join"
    const val MAC = "mac"
    
    // Bluetooth
    const val BT_SNIFF = "btsniff"
    const val BT_SPAM = "btspam"
    const val BT_WARDRIVE = "btwardrive"
    const val BT_SKIM = "btskim"
    const val BT_SPOOF_AT = "btspoofat"
    
    // GPS
    const val GPS = "gps"
    const val GPS_DATA = "gpsdata"
    const val NMEA = "nmea"
    const val GPS_POI = "gpspoi"
    const val GPS_TRACKER = "gpstracker"
    
    // Info
    const val INFO = "info"
    
    /**
     * Build scan command with options
     */
    fun buildScanApCommand(
        continuous: Boolean = false,
        timeout: Int? = null,
        channel: Int? = null
    ): String {
        val args = mutableListOf(SCAN_AP)
        if (continuous) args.add("-c")
        timeout?.let { args.add("-t"); args.add(it.toString()) }
        channel?.let { args.add("-ch"); args.add(it.toString()) }
        return args.joinToString(" ")
    }
    
    /**
     * Build attack command
     */
    fun buildAttackCommand(
        type: String,
        targetMac: String? = null,
        random: Boolean = false,
        timeout: Int? = null
    ): String {
        val args = mutableListOf(ATTACK, "-t", type)
        targetMac?.let { args.add("-m"); args.add(it) }
        if (random) args.add("-r")
        timeout?.let { args.add("-to"); args.add(it.toString()) }
        return args.joinToString(" ")
    }
    
    /**
     * Build select command
     */
    fun buildSelectCommand(
        apIndex: Int? = null,
        all: Boolean = false,
        deselect: Boolean = false
    ): String {
        val args = mutableListOf(SELECT)
        if (all) {
            args.add("-a")
        } else {
            apIndex?.let { args.add("-s"); args.add(it.toString()) }
        }
        if (deselect) args.add("-d")
        return args.joinToString(" ")
    }
    
    /**
     * Build SSID command
     */
    fun buildSsidCommand(
        ssid: String? = null,
        add: Boolean = true,
        remove: Boolean = false,
        random: Boolean = false,
        count: Int? = null
    ): String {
        val args = mutableListOf(SSID)
        if (add && ssid != null) {
            args.add("-a")
            args.add("\"$ssid\"")
        } else if (remove) {
            args.add("-r")
            ssid?.let { args.add("\"$it\"") }
        } else if (random) {
            args.add("-g")
            count?.let { args.add("-n"); args.add(it.toString()) }
        }
        return args.joinToString(" ")
    }
    
    /**
     * Build list command
     */
    fun buildListCommand(
        apList: Boolean = true,
        ssidList: Boolean = false,
        staList: Boolean = false
    ): String {
        val args = mutableListOf(LIST_AP)
        when {
            apList -> args.add("-a")
            ssidList -> args.add("-s")
            staList -> args.add("-c")
        }
        return args.joinToString(" ")
    }
    
    /**
     * Build clear command
     */
    fun buildClearCommand(
        clearAps: Boolean = false,
        clearSsids: Boolean = false,
        clearStations: Boolean = false
    ): String {
        val args = mutableListOf(CLEAR_AP)
        if (clearAps) args.add("-a")
        if (clearSsids) args.add("-s")
        if (clearStations) args.add("-c")
        return args.joinToString(" ")
    }
    
    /**
     * Build channel command
     */
    fun buildChannelCommand(channel: Int): String {
        return "$CHANNEL -s $channel"
    }
    
    /**
     * Build LED command
     */
    fun buildLedCommand(hexColor: String): String {
        return "$LED -s $hexColor"
    }
    
    /**
     * Build GPS command
     */
    fun buildGpsCommand(
        get: String? = null,
        track: Boolean = false,
        nmea: String? = null
    ): String {
        val args = mutableListOf(GPS)
        get?.let { args.add("-g"); args.add(it) }
        if (track) args.add("-t")
        nmea?.let { args.add("-n"); args.add(it) }
        return args.joinToString(" ")
    }
}
