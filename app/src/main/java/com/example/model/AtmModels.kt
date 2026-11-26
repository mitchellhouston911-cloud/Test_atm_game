package com.example.model

enum class ConsoleTab(val index: Int, val title: String, val shortCode: String) {
    CUSTOMER_BEZEL(0, "1. Customer Bezel", "FASCIA"),
    XFS_TELEMETRY(1, "2. XFS SP Telemetry", "XFS-SP"),
    CASH_CASSETTES(2, "3. Cash Cassettes", "CDM-310"),
    NDC_TRACE(3, "4. NDC / ISO-8583", "NDC+LOG")
}

enum class AtmScreenState {
    IDLE_WELCOME,
    CARD_READ_EMV,
    PIN_ENTRY,
    MAIN_MENU,
    FAST_CASH_SELECT,
    CUSTOM_AMOUNT_ENTRY,
    BALANCE_INQUIRY_RESULT,
    HOST_AUTH_TRANSMIT,
    DISPENSING_CASH,
    COLLECT_CASH_AND_CARD,
    TRANSACTION_COMPLETE,
    OUT_OF_SERVICE_FAULT,
    SUPERVISOR_SOP_MODE
}

enum class XfsDeviceHealth(val label: String, val xfsCode: String) {
    ONLINE("WFS_STAT_DEVONLINE", "OK"),
    WARN("WFS_STAT_DEVBUSY / LOW", "WARN"),
    FAULT("WFS_STAT_DEVHWERROR", "ERR")
}

data class XfsServiceProvider(
    val classCode: String,
    val logicalName: String,
    val hardwareModel: String,
    val firmwareVersion: String,
    val health: XfsDeviceHealth,
    val statusDetail: String,
    val extraMetricLabel: String,
    val extraMetricValue: String,
    val activeFaultCode: String? = null
)

data class CashCassette(
    val slotId: String,
    val physicalPosition: Int,
    val denominationUsd: Int,
    val currentNotes: Int,
    val maxCapacity: Int,
    val divertedCount: Int,
    val statusLabel: String,
    val isLocked: Boolean = true
) {
    val totalValueUsd: Int get() = currentNotes * denominationUsd
    val fillFraction: Float get() = (currentNotes.toFloat() / maxCapacity.toFloat()).coerceIn(0f, 1f)
}

data class TestCardProfile(
    val id: String,
    val label: String,
    val maskedPan: String,
    val aid: String,
    val mockLedgerBalance: Int,
    val defaultPin: String
)

data class NdcIsoMessage(
    val id: String,
    val timestamp: String,
    val direction: String, // "TX -> HOST" or "RX <- HOST" or "XFS-EVENT"
    val protocol: String,  // "NDC+ / ISO-8583" or "CEN/XFS 3.30"
    val mti: String,       // "0200", "0210", "0800", "WFS_EXEE"
    val summary: String,
    val stan: String,
    val de39ResponseCode: String,
    val rawHexAscii: String,
    val isError: Boolean = false
)

data class DispenseBreakdownItem(
    val slotId: String,
    val denomination: Int,
    val noteCount: Int
)

data class PrintedReceipt(
    val timestamp: String,
    val terminalId: String,
    val stan: String,
    val maskedPan: String,
    val aid: String,
    val transactionType: String,
    val dispensedAmount: Int,
    val remainingLedger: Int,
    val authCode: String,
    val cassetteBreakdown: List<DispenseBreakdownItem>
)

data class AtmTerminalUiState(
    val activeTab: ConsoleTab = ConsoleTab.CUSTOMER_BEZEL,
    val supervisorMaintMode: Boolean = false,
    val safeDoorLocked: Boolean = true,
    val upperChassisOpen: Boolean = false,
    val screenState: AtmScreenState = AtmScreenState.IDLE_WELCOME,
    val selectedCard: TestCardProfile = DEFAULT_TEST_CARDS.first(),
    val isCardInserted: Boolean = false,
    val enteredPin: String = "",
    val pinErrorText: String? = null,
    val customAmountBuffer: String = "",
    val cashErrorText: String? = null,
    val selectedAccountType: String = "CHECKING (10)",
    val lastDispensedAmount: Int = 0,
    val lastDispenseBreakdown: List<DispenseBreakdownItem> = emptyList(),
    val cashInShutterWaiting: Boolean = false,
    val cardInReaderWaitingTake: Boolean = false,
    val latestReceipt: PrintedReceipt? = null,
    val cassettes: List<CashCassette> = DEFAULT_CASSETTES,
    val rejectBinNotes: Int = 14,
    val purgeBinNotes: Int = 3,
    val serviceProviders: List<XfsServiceProvider> = DEFAULT_SERVICE_PROVIDERS,
    val ndcMessages: List<NdcIsoMessage> = DEFAULT_NDC_LOGS,
    val selectedNdcMessageId: String = DEFAULT_NDC_LOGS.first().id,
    val activeBannerAlert: String? = null,
    val stanCounter: Int = 482104
) {
    val totalVaultCashUsd: Int
        get() = cassettes.sumOf { it.totalValueUsd }

    val hasCriticalHardwareFault: Boolean
        get() = serviceProviders.any { it.health == XfsDeviceHealth.FAULT }
}

val DEFAULT_TEST_CARDS = listOf(
    TestCardProfile(
        id = "CARD_EMV_01",
        label = "EMV TEST PROFILE A (CORP)",
        maskedPan = "4532 •••• •••• 8841",
        aid = "A0000000031010",
        mockLedgerBalance = 4850,
        defaultPin = "1234"
    ),
    TestCardProfile(
        id = "CARD_EMV_02",
        label = "EMV TEST PROFILE B (PREMIER)",
        maskedPan = "5412 •••• •••• 3092",
        aid = "A0000000041010",
        mockLedgerBalance = 12400,
        defaultPin = "1234"
    ),
    TestCardProfile(
        id = "CARD_EMV_03",
        label = "EMV TEST PROFILE C (LOW BAL)",
        maskedPan = "4916 •••• •••• 1109",
        aid = "A0000000032010",
        mockLedgerBalance = 160,
        defaultPin = "1234"
    )
)

val DEFAULT_CASSETTES = listOf(
    CashCassette(
        slotId = "CAS-1",
        physicalPosition = 1,
        denominationUsd = 100,
        currentNotes = 420,
        maxCapacity = 2000,
        divertedCount = 3,
        statusLabel = "WFS_CDM_STATCUOK"
    ),
    CashCassette(
        slotId = "CAS-2",
        physicalPosition = 2,
        denominationUsd = 50,
        currentNotes = 650,
        maxCapacity = 2000,
        divertedCount = 4,
        statusLabel = "WFS_CDM_STATCUOK"
    ),
    CashCassette(
        slotId = "CAS-3",
        physicalPosition = 3,
        denominationUsd = 20,
        currentNotes = 1180,
        maxCapacity = 2000,
        divertedCount = 5,
        statusLabel = "WFS_CDM_STATCUOK"
    ),
    CashCassette(
        slotId = "CAS-4",
        physicalPosition = 4,
        denominationUsd = 10,
        currentNotes = 180,
        maxCapacity = 2000,
        divertedCount = 2,
        statusLabel = "WFS_CDM_STATCULOW"
    )
)

val DEFAULT_SERVICE_PROVIDERS = listOf(
    XfsServiceProvider(
        classCode = "CDM",
        logicalName = "CurrencyDispenser1",
        hardwareModel = "NCR S2 Pick Module 4-Cassette",
        firmwareVersion = "FW v4.18.02-XFS",
        health = XfsDeviceHealth.ONLINE,
        statusDetail = "WFS_CDM_DEVONLINE | SHUTTER_CLOSED | TRANSPORT_OK",
        extraMetricLabel = "Belt Vacuum / Pick Sensor",
        extraMetricValue = "68.4 kPa / 99.7% Pick Accuracy"
    ),
    XfsServiceProvider(
        classCode = "IDC",
        logicalName = "CardReader1",
        hardwareModel = "Motorized Hybrid EMV + Anti-Skim",
        firmwareVersion = "FW v2.09.14-EMV",
        health = XfsDeviceHealth.ONLINE,
        statusDetail = "WFS_IDC_MEDIAENTERING_READY | CHIP_POWER_IDLE",
        extraMetricLabel = "Jitter / Anti-Skim RF Field",
        extraMetricValue = "ACTIVE (24.1 dBm Clean)"
    ),
    XfsServiceProvider(
        classCode = "PIN",
        logicalName = "PinPad1",
        hardwareModel = "PCI-PTS 6.x EPP Hardware Cryptoprocessor",
        firmwareVersion = "FW v6.41.00-TR31",
        health = XfsDeviceHealth.ONLINE,
        statusDetail = "WFS_PIN_DEVONLINE | DUKPT_KEY_LOADED | TAMPER_OK",
        extraMetricLabel = "ANSI X9.24 DUKPT KSN",
        extraMetricValue = "FFFF9876543210E0004A"
    ),
    XfsServiceProvider(
        classCode = "PTR",
        logicalName = "ReceiptPrinter1",
        hardwareModel = "80mm Thermal Presenter + Auto-Cutter",
        firmwareVersion = "FW v3.11.08-PTR",
        health = XfsDeviceHealth.ONLINE,
        statusDetail = "WFS_PTR_PAPERFULL | TONER_OK | RETRACT_BIN_12%",
        extraMetricLabel = "Thermal Roll Remaining",
        extraMetricValue = "84% (~410 Receipts)"
    ),
    XfsServiceProvider(
        classCode = "SIU",
        logicalName = "SensorsIndicators1",
        hardwareModel = "Cabinet Fascia & Safe Telemetry Hub",
        firmwareVersion = "FW v1.92.00-SIU",
        health = XfsDeviceHealth.ONLINE,
        statusDetail = "SAFE_DOOR_LOCKED | SEISMIC_NORMAL | FASCIA_GUIDELIGHTS",
        extraMetricLabel = "Internal Temp / UPS Rail",
        extraMetricValue = "29.4°C / 24.08V DC Nominal"
    )
)

val DEFAULT_NDC_LOGS = listOf(
    NdcIsoMessage(
        id = "MSG-104",
        timestamp = "18:42:10.412",
        direction = "RX <- HOST",
        protocol = "NDC+ / ISO-8583",
        mti = "0810",
        summary = "Network Management Echo Response (In-Service Ready)",
        stan = "482103",
        de39ResponseCode = "00 (APPROVED)",
        rawHexAscii = "30 38 31 30 82 20 00 00 02 00 00 00 04 00 00 00\n" +
            "DE007: 1125184210 | DE011: 482103 | DE039: 00\n" +
            "DE041: ATM04201   | DE070: 301 (ECHO_TEST_OK)"
    ),
    NdcIsoMessage(
        id = "MSG-103",
        timestamp = "18:42:10.108",
        direction = "TX -> HOST",
        protocol = "NDC+ / ISO-8583",
        mti = "0800",
        summary = "Solicited Status Heartbeat & Cassette Fitness Poll",
        stan = "482103",
        de39ResponseCode = "--",
        rawHexAscii = "30 38 30 30 82 20 00 00 00 00 00 00 04 00 00 00\n" +
            "NDC_CMD: 11<FS>042<FS>B<FS>1 (IN_SERVICE | CAS_1-3_OK | CAS_4_LOW)\n" +
            "DE011: 482103 | DE041: ATM04201 | DE070: 301"
    ),
    NdcIsoMessage(
        id = "MSG-102",
        timestamp = "18:39:51.894",
        direction = "XFS-EVENT",
        protocol = "CEN/XFS 3.30",
        mti = "WFS_SYSE",
        summary = "WFSOpen() Completed Across 5 Logical Service Providers",
        stan = "482102",
        de39ResponseCode = "WFS_SUCCESS (0)",
        rawHexAscii = "hService=0x01A4 | lpszLogicalName=\"CurrencyDispenser1\"\n" +
            "dwVersionsRequired=0x0003001E (XFS 3.30)\n" +
            "Result: WFS_SUCCESS | HPROVIDER_READY"
    )
)
