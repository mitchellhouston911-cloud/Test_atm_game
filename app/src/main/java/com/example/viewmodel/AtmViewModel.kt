package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AtmScreenState
import com.example.model.AtmTerminalUiState
import com.example.model.ConsoleTab
import com.example.model.DEFAULT_CASSETTES
import com.example.model.DEFAULT_SERVICE_PROVIDERS
import com.example.model.DEFAULT_TEST_CARDS
import com.example.model.DispenseBreakdownItem
import com.example.model.NdcIsoMessage
import com.example.model.PrintedReceipt
import com.example.model.TestCardProfile
import com.example.model.XfsDeviceHealth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AtmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AtmTerminalUiState())
    val uiState: StateFlow<AtmTerminalUiState> = _uiState.asStateFlow()

    private fun nowTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        return sdf.format(Date())
    }

    fun selectTab(tab: ConsoleTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun navigateBackToFascia() {
        _uiState.update { it.copy(activeTab = ConsoleTab.CUSTOMER_BEZEL) }
    }

    fun toggleSupervisorKey() {
        val enteringMaint = !_uiState.value.supervisorMaintMode
        _uiState.update { state ->
            val nextScreen = when {
                enteringMaint -> AtmScreenState.SUPERVISOR_SOP_MODE
                state.hasCriticalHardwareFault -> AtmScreenState.OUT_OF_SERVICE_FAULT
                state.isCardInserted -> AtmScreenState.MAIN_MENU
                else -> AtmScreenState.IDLE_WELCOME
            }
            state.copy(
                supervisorMaintMode = enteringMaint,
                screenState = nextScreen,
                activeBannerAlert = if (enteringMaint) {
                    "SUPERVISOR KEY ROTATED TO SOP MAINTENANCE — CUSTOMER FASCIA LOCKED"
                } else {
                    null
                }
            )
        }
        appendXfsEvent(
            mti = "WFS_SRVE",
            summary = if (enteringMaint) {
                "WFS_SRVE_SIU_PORT_STATUS: OPERATOR_SWITCH -> SUPERVISOR_SOP"
            } else {
                "WFS_SRVE_SIU_PORT_STATUS: OPERATOR_SWITCH -> NORMAL_IN_SERVICE"
            },
            rawDetail = "SIU Port[WFS_SIU_OPERATORSWITCH] = ${if (enteringMaint) "WFS_SIU_SUPERVISOR" else "WFS_SIU_RUN"}\n" +
                "Terminal Mode Transition Completed"
        )
    }

    fun selectTestCardProfile(card: TestCardProfile) {
        _uiState.update {
            it.copy(
                selectedCard = card,
                pinErrorText = null,
                cashErrorText = null
            )
        }
    }

    fun insertEmvCard() {
        val current = _uiState.value
        if (current.supervisorMaintMode) {
            _uiState.update {
                it.copy(activeBannerAlert = "Cannot insert card while Supervisor Key is in SOP Maintenance Mode.")
            }
            return
        }
        if (current.hasCriticalHardwareFault) {
            _uiState.update {
                it.copy(
                    screenState = AtmScreenState.OUT_OF_SERVICE_FAULT,
                    activeBannerAlert = "Terminal Out-Of-Service due to active XFS hardware fault."
                )
            }
            return
        }
        if (current.isCardInserted) return

        _uiState.update {
            it.copy(
                isCardInserted = true,
                cardInReaderWaitingTake = false,
                enteredPin = "",
                pinErrorText = null,
                cashErrorText = null,
                screenState = AtmScreenState.CARD_READ_EMV,
                activeBannerAlert = null
            )
        }

        appendXfsEvent(
            mti = "WFS_EXEE",
            summary = "WFS_CMD_IDC_READ_RAW_DATA -> EMV ATR & AID ${current.selectedCard.aid}",
            rawDetail = "LogicalName: CardReader1 (IDC)\n" +
                "ATR: 3B 68 00 00 80 31 E0 6B 05 12 01 02 90 00\n" +
                "PAN: ${current.selectedCard.maskedPan} | AID: ${current.selectedCard.aid}"
        )

        viewModelScope.launch {
            delay(550)
            _uiState.update { state ->
                if (state.isCardInserted && state.screenState == AtmScreenState.CARD_READ_EMV) {
                    state.copy(screenState = AtmScreenState.PIN_ENTRY)
                } else {
                    state
                }
            }
        }
    }

    fun ejectCard() {
        val current = _uiState.value
        if (!current.isCardInserted && !current.cardInReaderWaitingTake) return

        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "WFS_CMD_IDC_EJECT_CARD -> Media Ejected & Taken",
            rawDetail = "LogicalName: CardReader1 (IDC)\n" +
                "Event: WFS_SRVE_IDC_MEDIAREMOVED | Shutter Closed"
        )

        _uiState.update {
            it.copy(
                isCardInserted = false,
                cardInReaderWaitingTake = false,
                cashInShutterWaiting = false,
                enteredPin = "",
                customAmountBuffer = "",
                pinErrorText = null,
                cashErrorText = null,
                screenState = if (it.supervisorMaintMode) {
                    AtmScreenState.SUPERVISOR_SOP_MODE
                } else if (it.hasCriticalHardwareFault) {
                    AtmScreenState.OUT_OF_SERVICE_FAULT
                } else {
                    AtmScreenState.IDLE_WELCOME
                }
            )
        }
    }

    fun onEppDigitPressed(digit: String) {
        val state = _uiState.value
        when (state.screenState) {
            AtmScreenState.PIN_ENTRY -> {
                if (state.enteredPin.length < 4) {
                    val updatedPin = state.enteredPin + digit
                    _uiState.update {
                        it.copy(enteredPin = updatedPin, pinErrorText = null)
                    }
                }
            }
            AtmScreenState.CUSTOM_AMOUNT_ENTRY -> {
                if (state.customAmountBuffer.length < 4) {
                    val updatedAmt = (state.customAmountBuffer + digit).trimStart('0')
                    _uiState.update {
                        it.copy(customAmountBuffer = updatedAmt, cashErrorText = null)
                    }
                }
            }
            else -> Unit
        }
    }

    fun onEppClearPressed() {
        val state = _uiState.value
        when (state.screenState) {
            AtmScreenState.PIN_ENTRY -> {
                _uiState.update { it.copy(enteredPin = "", pinErrorText = null) }
            }
            AtmScreenState.CUSTOM_AMOUNT_ENTRY -> {
                _uiState.update { it.copy(customAmountBuffer = "", cashErrorText = null) }
            }
            else -> Unit
        }
    }

    fun onEppCancelPressed() {
        val state = _uiState.value
        if (state.isCardInserted || state.cardInReaderWaitingTake) {
            ejectCard()
        } else {
            _uiState.update {
                it.copy(
                    enteredPin = "",
                    customAmountBuffer = "",
                    pinErrorText = null,
                    cashErrorText = null
                )
            }
        }
    }

    fun onEppEnterPressed() {
        val state = _uiState.value
        when (state.screenState) {
            AtmScreenState.PIN_ENTRY -> {
                if (state.enteredPin.length < 4) {
                    _uiState.update {
                        it.copy(pinErrorText = "ENTER 4-DIGIT PIN ON EPP KEYPAD (TRY 1234)")
                    }
                } else {
                    appendXfsEvent(
                        mti = "WFS_CMD",
                        summary = "WFS_CMD_PIN_GET_PINBLOCK (ISO-9564 Format 0 / TR-31)",
                        rawDetail = "LogicalName: PinPad1 (EPP)\n" +
                            "Encrypted PinBlock: 9A 4E 7C 11 08 D3 B2 5F\n" +
                            "KSN: FFFF9876543210E0004A"
                    )
                    _uiState.update {
                        it.copy(
                            screenState = AtmScreenState.MAIN_MENU,
                            pinErrorText = null,
                            cashErrorText = null
                        )
                    }
                }
            }
            AtmScreenState.CUSTOM_AMOUNT_ENTRY -> {
                val amount = state.customAmountBuffer.toIntOrNull() ?: 0
                requestCashDispense(amount)
            }
            else -> Unit
        }
    }

    /**
     * FDK Softkeys: L1..L4 on left bezel, R1..R4 on right bezel
     */
    fun onFdkPressed(keyId: String) {
        val state = _uiState.value
        if (state.supervisorMaintMode) {
            when (keyId) {
                "L1" -> runDiagnosticSelfTest()
                "L2" -> replenishAllCassettes()
                "R1" -> clearAllFaults()
                "R4" -> toggleSupervisorKey()
            }
            return
        }

        when (state.screenState) {
            AtmScreenState.IDLE_WELCOME -> {
                if (keyId == "L4" || keyId == "R4") {
                    insertEmvCard()
                }
            }
            AtmScreenState.PIN_ENTRY -> {
                if (keyId == "R4") {
                    onEppEnterPressed()
                } else if (keyId == "L4") {
                    ejectCard()
                }
            }
            AtmScreenState.MAIN_MENU -> {
                when (keyId) {
                    "L1" -> _uiState.update {
                        it.copy(screenState = AtmScreenState.FAST_CASH_SELECT, cashErrorText = null)
                    }
                    "L2" -> _uiState.update {
                        it.copy(
                            screenState = AtmScreenState.CUSTOM_AMOUNT_ENTRY,
                            customAmountBuffer = "",
                            cashErrorText = null
                        )
                    }
                    "L3" -> performBalanceInquiry()
                    "L4" -> ejectCard()
                    "R1" -> requestCashDispense(100)
                    "R2" -> requestCashDispense(200)
                    "R3" -> toggleAccountType()
                    "R4" -> printDiagnosticMiniStatement()
                }
            }
            AtmScreenState.FAST_CASH_SELECT -> {
                when (keyId) {
                    "L1" -> requestCashDispense(20)
                    "L2" -> requestCashDispense(50)
                    "L3" -> requestCashDispense(100)
                    "L4" -> _uiState.update { it.copy(screenState = AtmScreenState.MAIN_MENU, cashErrorText = null) }
                    "R1" -> requestCashDispense(200)
                    "R2" -> requestCashDispense(300)
                    "R3" -> requestCashDispense(500)
                    "R4" -> _uiState.update {
                        it.copy(
                            screenState = AtmScreenState.CUSTOM_AMOUNT_ENTRY,
                            customAmountBuffer = "",
                            cashErrorText = null
                        )
                    }
                }
            }
            AtmScreenState.CUSTOM_AMOUNT_ENTRY -> {
                when (keyId) {
                    "L1" -> _uiState.update { it.copy(customAmountBuffer = "40", cashErrorText = null) }
                    "L2" -> _uiState.update { it.copy(customAmountBuffer = "80", cashErrorText = null) }
                    "L3" -> _uiState.update { it.copy(customAmountBuffer = "150", cashErrorText = null) }
                    "L4" -> _uiState.update { it.copy(screenState = AtmScreenState.MAIN_MENU, cashErrorText = null) }
                    "R3" -> _uiState.update { it.copy(customAmountBuffer = "", cashErrorText = null) }
                    "R4" -> onEppEnterPressed()
                }
            }
            AtmScreenState.BALANCE_INQUIRY_RESULT -> {
                when (keyId) {
                    "L1" -> _uiState.update { it.copy(screenState = AtmScreenState.FAST_CASH_SELECT) }
                    "L4" -> _uiState.update { it.copy(screenState = AtmScreenState.MAIN_MENU) }
                    "R4" -> ejectCard()
                }
            }
            AtmScreenState.COLLECT_CASH_AND_CARD -> {
                when (keyId) {
                    "L4", "R4" -> collectShutterCashAndCard()
                }
            }
            AtmScreenState.TRANSACTION_COMPLETE -> {
                when (keyId) {
                    "L1" -> _uiState.update { it.copy(screenState = AtmScreenState.MAIN_MENU) }
                    "L4", "R4" -> ejectCard()
                }
            }
            AtmScreenState.OUT_OF_SERVICE_FAULT -> {
                when (keyId) {
                    "R4" -> clearAllFaults()
                    "L4" -> selectTab(ConsoleTab.XFS_TELEMETRY)
                }
            }
            else -> Unit
        }
    }

    private fun toggleAccountType() {
        _uiState.update { state ->
            val nextAcct = if (state.selectedAccountType.startsWith("CHECKING")) {
                "SAVINGS (20)"
            } else {
                "CHECKING (10)"
            }
            state.copy(selectedAccountType = nextAcct)
        }
    }

    private fun performBalanceInquiry() {
        val state = _uiState.value
        val nextStan = (state.stanCounter + 1)
        val stanStr = nextStan.toString()

        appendNdcIsoPair(
            stan = stanStr,
            reqMti = "0200",
            reqSummary = "ISO-8583 Balance Inquiry (ProcCode 310000 | ${state.selectedCard.maskedPan})",
            reqHex = "30 32 30 30 F2 38 40 01 08 E0 80 00 00 00 00 00\n" +
                "DE002: ${state.selectedCard.maskedPan} | DE003: 310000\n" +
                "DE011: $stanStr | DE041: ATM04201 | DE052: 9A4E7C1108D3B25F",
            respMti = "0210",
            respSummary = "ISO-8583 Balance Inquiry Approved (DE39=00 | Ledger $${state.selectedCard.mockLedgerBalance})",
            respCode = "00 (APPROVED)",
            respHex = "30 32 31 30 F2 38 40 01 0A E0 80 00 00 00 00 00\n" +
                "DE003: 310000 | DE011: $stanStr | DE038: AUTH91\n" +
                "DE039: 00 | DE054: 1002840C000000${state.selectedCard.mockLedgerBalance}00"
        )

        val receipt = PrintedReceipt(
            timestamp = nowTime(),
            terminalId = "WIN10-IOT-ATM-042",
            stan = stanStr,
            maskedPan = state.selectedCard.maskedPan,
            aid = state.selectedCard.aid,
            transactionType = "BALANCE INQUIRY (${state.selectedAccountType})",
            dispensedAmount = 0,
            remainingLedger = state.selectedCard.mockLedgerBalance,
            authCode = "AUTH91",
            cassetteBreakdown = emptyList()
        )

        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                screenState = AtmScreenState.BALANCE_INQUIRY_RESULT,
                latestReceipt = receipt
            )
        }
    }

    private fun printDiagnosticMiniStatement() {
        val state = _uiState.value
        val nextStan = state.stanCounter + 1
        val stanStr = nextStan.toString()
        val receipt = PrintedReceipt(
            timestamp = nowTime(),
            terminalId = "WIN10-IOT-ATM-042",
            stan = stanStr,
            maskedPan = state.selectedCard.maskedPan,
            aid = state.selectedCard.aid,
            transactionType = "XFS PTR MINI-STATEMENT",
            dispensedAmount = 0,
            remainingLedger = state.selectedCard.mockLedgerBalance,
            authCode = "STMT04",
            cassetteBreakdown = emptyList()
        )
        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "WFS_CMD_PTR_PRINT_FORM -> 80mm Thermal Receipt Presented",
            rawDetail = "LogicalName: ReceiptPrinter1 (PTR)\nFormName: \"NDC_RECEIPT_80MM\" | CutPaper: TRUE"
        )
        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                latestReceipt = receipt,
                activeBannerAlert = "80mm Thermal Receipt Printed to Fascia Slot (STAN #$stanStr)"
            )
        }
    }

    fun requestCashDispense(amountUsd: Int) {
        val state = _uiState.value
        if (amountUsd <= 0 || amountUsd % 10 != 0) {
            _uiState.update {
                it.copy(cashErrorText = "INVALID AMOUNT: MUST BE A POSITIVE MULTIPLE OF $10")
            }
            return
        }
        if (amountUsd > 2000) {
            _uiState.update {
                it.copy(cashErrorText = "EXCEEDS CDM SINGLE-BUNDLE SHUTTER LIMIT ($2,000 MAX)")
            }
            return
        }
        if (amountUsd > state.selectedCard.mockLedgerBalance) {
            val nextStan = state.stanCounter + 1
            appendNdcIsoPair(
                stan = nextStan.toString(),
                reqMti = "0200",
                reqSummary = "ISO-8583 Cash Withdrawal Request $$amountUsd",
                reqHex = "DE003: 010000 | DE004: ${String.format(Locale.US, "%012d", amountUsd * 100)} | DE011: $nextStan",
                respMti = "0210",
                respSummary = "Host Declined: DE39=51 Insufficient Test Profile Funds",
                respCode = "51 (DECLINED)",
                respHex = "DE011: $nextStan | DE039: 51 (INSUFFICIENT_FUNDS)",
                isError = true
            )
            _uiState.update {
                it.copy(
                    stanCounter = nextStan,
                    cashErrorText = "HOST DECLINED (DE39=51): EXCEEDS TEST PROFILE BALANCE ($${state.selectedCard.mockLedgerBalance})"
                )
            }
            return
        }

        // Compute greedy cassette mix using available notes across CAS-1..CAS-4
        val mix = calculateCassetteMix(amountUsd, state.cassettes)
        if (mix == null) {
            _uiState.update {
                it.copy(cashErrorText = "WFS_ERR_CDM_NOTDISPENSABLE: INSUFFICIENT DENOMINATIONS IN CASSETTES")
            }
            return
        }

        val nextStan = state.stanCounter + 1
        val stanStr = nextStan.toString()

        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                screenState = AtmScreenState.HOST_AUTH_TRANSMIT,
                cashErrorText = null
            )
        }

        viewModelScope.launch {
            delay(450)
            appendNdcIsoPair(
                stan = stanStr,
                reqMti = "0200",
                reqSummary = "NDC+ / ISO-8583 Withdrawal Request $$amountUsd (${state.selectedAccountType})",
                reqHex = "30 32 30 30 F2 38 44 81 28 E0 80 00 00 00 00 00\n" +
                    "DE002: ${state.selectedCard.maskedPan} | DE003: 011000\n" +
                    "DE004: ${String.format(Locale.US, "%012d", amountUsd * 100)} | DE011: $stanStr\n" +
                    "DE041: ATM04201 | DE049: 840 (USD) | DE052: DUKPT_PINBLOCK_OK",
                respMti = "0210",
                respSummary = "ISO-8583 Financial Authorization Approved (DE39=00 | Mix: ${
                    mix.joinToString(" + ") { "${it.noteCount}x$${it.denomination}" }
                })",
                respCode = "00 (APPROVED)",
                respHex = "30 32 31 30 F2 38 44 81 2A E0 80 00 00 00 00 00\n" +
                    "DE004: ${String.format(Locale.US, "%012d", amountUsd * 100)} | DE011: $stanStr\n" +
                    "DE038: OK${stanStr.takeLast(4)} | DE039: 00 (APPROVED)\n" +
                    "NDC_CMD: 4<FS>00<FS>$stanStr<FS>DISPENSE_CAS_MIX"
            )

            _uiState.update { it.copy(screenState = AtmScreenState.DISPENSING_CASH) }

            delay(650)

            // Deduct notes from cassettes and update card balance
            _uiState.update { current ->
                val updatedCassettes = current.cassettes.map { cas ->
                    val used = mix.find { it.slotId == cas.slotId }?.noteCount ?: 0
                    val remaining = (cas.currentNotes - used).coerceAtLeast(0)
                    val newStatus = when {
                        remaining == 0 -> "WFS_CDM_STATCUEMPTY"
                        remaining < 250 -> "WFS_CDM_STATCULOW"
                        else -> "WFS_CDM_STATCUOK"
                    }
                    cas.copy(currentNotes = remaining, statusLabel = newStatus)
                }

                val newBalance = (current.selectedCard.mockLedgerBalance - amountUsd).coerceAtLeast(0)
                val updatedCard = current.selectedCard.copy(mockLedgerBalance = newBalance)

                val receipt = PrintedReceipt(
                    timestamp = nowTime(),
                    terminalId = "WIN10-IOT-ATM-042",
                    stan = stanStr,
                    maskedPan = updatedCard.maskedPan,
                    aid = updatedCard.aid,
                    transactionType = "CASH WITHDRAWAL (${current.selectedAccountType})",
                    dispensedAmount = amountUsd,
                    remainingLedger = newBalance,
                    authCode = "OK${stanStr.takeLast(4)}",
                    cassetteBreakdown = mix
                )

                current.copy(
                    cassettes = updatedCassettes,
                    selectedCard = updatedCard,
                    lastDispensedAmount = amountUsd,
                    lastDispenseBreakdown = mix,
                    cashInShutterWaiting = true,
                    cardInReaderWaitingTake = true,
                    latestReceipt = receipt,
                    screenState = AtmScreenState.COLLECT_CASH_AND_CARD
                )
            }

            appendXfsEvent(
                mti = "WFS_CMD",
                summary = "WFS_CMD_CDM_DISPENSE & WFS_CMD_CDM_PRESENT -> $$amountUsd at Shutter",
                rawDetail = "LogicalName: CurrencyDispenser1 (CDM)\n" +
                    "MixAlgorithm: WFS_CDM_MIX_MINIMUM_NUMBER_OF_BILLS\n" +
                    "Breakdown: ${mix.joinToString(", ") { "${it.slotId}: ${it.noteCount}x$${it.denomination}" }}\n" +
                    "ShutterState: WFS_CDM_SHTOPEN | Presenter: WFS_CDM_PRPRESENTED"
            )
        }
    }

    private fun calculateCassetteMix(
        amountUsd: Int,
        cassettes: List<com.example.model.CashCassette>
    ): List<DispenseBreakdownItem>? {
        var remaining = amountUsd
        val result = mutableListOf<DispenseBreakdownItem>()
        for (cas in cassettes.sortedByDescending { it.denominationUsd }) {
            if (remaining <= 0) break
            val maxNotesNeeded = remaining / cas.denominationUsd
            val notesToPick = minOf(maxNotesNeeded, cas.currentNotes)
            if (notesToPick > 0) {
                result.add(
                    DispenseBreakdownItem(
                        slotId = cas.slotId,
                        denomination = cas.denominationUsd,
                        noteCount = notesToPick
                    )
                )
                remaining -= notesToPick * cas.denominationUsd
            }
        }
        return if (remaining == 0) result else null
    }

    fun collectShutterCashAndCard() {
        appendXfsEvent(
            mti = "WFS_SRVE",
            summary = "WFS_SRVE_CDM_ITEMSTAKEN & WFS_SRVE_IDC_MEDIAREMOVED",
            rawDetail = "Customer collected cash bundle and EMV card.\n" +
                "CDM Shutter State -> WFS_CDM_SHTCLOSED"
        )
        _uiState.update {
            it.copy(
                cashInShutterWaiting = false,
                cardInReaderWaitingTake = false,
                isCardInserted = false,
                enteredPin = "",
                customAmountBuffer = "",
                screenState = AtmScreenState.IDLE_WELCOME,
                activeBannerAlert = "Cash Bundle ($$${it.lastDispensedAmount}) & EMV Card Collected — Shutter Closed"
            )
        }
    }

    // ================= VIEW 2: CEN/XFS HARDWARE TELEMETRY & FAULT INJECTION =================

    fun executeXfsCommand(classCode: String, commandName: String) {
        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "$commandName on $classCode Service Provider -> WFS_SUCCESS",
            rawDetail = "SPI Call: WFSAsyncExecute(hService[$classCode], $commandName)\n" +
                "Completion Latency: 18 ms | hResult: WFS_SUCCESS (0)"
        )
        _uiState.update { state ->
            val updatedProviders = state.serviceProviders.map { sp ->
                if (sp.classCode == classCode) {
                    sp.copy(
                        health = XfsDeviceHealth.ONLINE,
                        activeFaultCode = null,
                        statusDetail = "WFS_${classCode}_DEVONLINE | LAST_CMD: $commandName"
                    )
                } else {
                    sp
                }
            }
            val stillFaulted = updatedProviders.any { it.health == XfsDeviceHealth.FAULT }
            state.copy(
                serviceProviders = updatedProviders,
                screenState = if (!stillFaulted && state.screenState == AtmScreenState.OUT_OF_SERVICE_FAULT) {
                    if (state.isCardInserted) AtmScreenState.MAIN_MENU else AtmScreenState.IDLE_WELCOME
                } else {
                    state.screenState
                },
                activeBannerAlert = "Executed $commandName on $classCode -> WFS_SUCCESS"
            )
        }
    }

    fun injectHardwareFault(classCode: String, faultCode: String, description: String) {
        appendXfsEvent(
            mti = "WFS_SYSE",
            summary = "HARDWARE FAULT INJECTED [$classCode]: $faultCode",
            rawDetail = "Event: WFS_SYSE_HARDWARE_ERROR\n" +
                "Class: $classCode | ErrorCode: $faultCode\n" +
                "Detail: $description\n" +
                "Action Required: WFS_CMD_${classCode}_RESET or Clear Fault",
            isError = true
        )
        _uiState.update { state ->
            val updatedProviders = state.serviceProviders.map { sp ->
                if (sp.classCode == classCode) {
                    sp.copy(
                        health = XfsDeviceHealth.FAULT,
                        activeFaultCode = faultCode,
                        statusDetail = "$faultCode | $description"
                    )
                } else {
                    sp
                }
            }
            state.copy(
                serviceProviders = updatedProviders,
                screenState = if (!state.supervisorMaintMode) {
                    AtmScreenState.OUT_OF_SERVICE_FAULT
                } else {
                    state.screenState
                },
                activeBannerAlert = "XFS FAULT [$classCode]: $faultCode ($description)"
            )
        }
    }

    fun clearAllFaults() {
        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "WFS_CMD_ALL_RESET -> All 5 CEN/XFS Service Providers Online",
            rawDetail = "Broadcast Reset across CDM310, IDC310, PIN310, PTR310, SIU310.\n" +
                "All transport rollers clear, shutters closed, DUKPT ready."
        )
        _uiState.update { state ->
            state.copy(
                serviceProviders = DEFAULT_SERVICE_PROVIDERS,
                screenState = when {
                    state.supervisorMaintMode -> AtmScreenState.SUPERVISOR_SOP_MODE
                    state.isCardInserted -> AtmScreenState.MAIN_MENU
                    else -> AtmScreenState.IDLE_WELCOME
                },
                activeBannerAlert = "All CEN/XFS Hardware Faults Cleared — Terminal In-Service"
            )
        }
    }

    fun runDiagnosticSelfTest() {
        val nextStan = _uiState.value.stanCounter + 1
        appendNdcIsoPair(
            stan = nextStan.toString(),
            reqMti = "0800",
            reqSummary = "CEN/XFS Full Subsystem Self-Test & NDC Fitness Report",
            reqHex = "WFSGetInfo(WFS_INF_CDM_STATUS, WFS_INF_IDC_STATUS, WFS_INF_PIN_STATUS)\n" +
                "NDC Fitness Frame: 22<FS>042<FS>HW_DIAG_PASS",
            respMti = "0810",
            respSummary = "Host Acknowledged Full Hardware Self-Test (5/5 SPs WFS_SUCCESS)",
            respCode = "00 (DIAG_OK)",
            respHex = "DE011: $nextStan | DE039: 00 | DE070: 301 (ALL_MODULES_HEALTHY)"
        )
        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                activeBannerAlert = "CEN/XFS 3.30 Diagnostic Self-Test Passed across CDM, IDC, PIN, PTR, SIU"
            )
        }
    }

    // ================= VIEW 3: CASH CASSETTES & VAULT MANAGEMENT =================

    fun replenishSingleCassette(slotId: String, addNotes: Int = 500) {
        _uiState.update { state ->
            val updated = state.cassettes.map { cas ->
                if (cas.slotId == slotId) {
                    val newCount = (cas.currentNotes + addNotes).coerceAtMost(cas.maxCapacity)
                    cas.copy(
                        currentNotes = newCount,
                        statusLabel = if (newCount >= 250) "WFS_CDM_STATCUOK" else "WFS_CDM_STATCULOW"
                    )
                } else {
                    cas
                }
            }
            state.copy(
                cassettes = updated,
                activeBannerAlert = "WFS_CMD_CDM_SET_CASH_UNIT_INFO: Loaded +$addNotes notes into $slotId"
            )
        }
        appendXfsEvent(
            mti = "WFS_SRVE",
            summary = "WFS_SRVE_CDM_CASHUNITINFOCHANGED ($slotId Replenished +$addNotes notes)",
            rawDetail = "LogicalName: CurrencyDispenser1 (CDM)\n" +
                "Cassette: $slotId | Action: Replenish +$addNotes notes"
        )
    }

    fun replenishAllCassettes() {
        _uiState.update { state ->
            val updated = state.cassettes.map { cas ->
                cas.copy(
                    currentNotes = 1600,
                    statusLabel = "WFS_CDM_STATCUOK"
                )
            }
            state.copy(
                cassettes = updated,
                rejectBinNotes = 0,
                purgeBinNotes = 0,
                activeBannerAlert = "All 4 Cash Cassettes Replenished to 1,600 Notes & Reject/Purge Bins Emptied"
            )
        }
        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "WFS_CMD_CDM_END_EXCHANGE -> All 4 Cassettes Filled (1,600 Notes Each)",
            rawDetail = "CAS-1 ($100): 1600 | CAS-2 ($50): 1600 | CAS-3 ($20): 1600 | CAS-4 ($10): 1600\n" +
                "Total Vault Inventory: $296,000.00"
        )
    }

    fun testPickToRejectBin() {
        _uiState.update { state ->
            val updated = state.cassettes.map { cas ->
                val nextNotes = (cas.currentNotes - 1).coerceAtLeast(0)
                cas.copy(
                    currentNotes = nextNotes,
                    divertedCount = cas.divertedCount + 1
                )
            }
            state.copy(
                cassettes = updated,
                rejectBinNotes = state.rejectBinNotes + 4,
                activeBannerAlert = "WFS_CMD_CDM_TEST_CASH_UNITS: 1 Note Picked from Each Cassette into Reject Bin"
            )
        }
        appendXfsEvent(
            mti = "WFS_CMD",
            summary = "WFS_CMD_CDM_TEST_CASH_UNITS -> 4 Test Notes Diverted to REJECT-BIN",
            rawDetail = "Pick sensors verified thickness & optical skew on CAS-1..CAS-4.\n" +
                "Diverted 4 notes to WFS_CDM_TYPEREJECTCASSETTE."
        )
    }

    fun toggleSafeDoorLock() {
        val nextLocked = !_uiState.value.safeDoorLocked
        _uiState.update {
            it.copy(
                safeDoorLocked = nextLocked,
                activeBannerAlert = if (nextLocked) {
                    "CEN L1 Safe Door Bolted & Relocked (WFS_SIU_CLOSED)"
                } else {
                    "CEN L1 Safe Door Unbolted for Cassette Exchange (WFS_SIU_OPEN)"
                }
            )
        }
        appendXfsEvent(
            mti = "WFS_SRVE",
            summary = "WFS_SRVE_SIU_PORT_STATUS: SAFE_DOOR -> ${if (nextLocked) "LOCKED" else "OPEN"}",
            rawDetail = "SIU Sensor[WFS_SIU_SAFEDOOR] = ${if (nextLocked) "WFS_SIU_CLOSED" else "WFS_SIU_OPEN"}"
        )
    }

    // ================= VIEW 4: NDC+ / ISO-8583 PACKET INSPECTOR =================

    fun selectNdcMessage(messageId: String) {
        _uiState.update { it.copy(selectedNdcMessageId = messageId) }
    }

    fun sendIsoEchoTest() {
        val nextStan = _uiState.value.stanCounter + 1
        val stanStr = nextStan.toString()
        appendNdcIsoPair(
            stan = stanStr,
            reqMti = "0800",
            reqSummary = "ISO-8583 Network Management Echo Test (DE70=301)",
            reqHex = "30 38 30 30 82 20 00 00 00 00 00 00 04 00 00 00\n" +
                "DE007: ${nowTime()} | DE011: $stanStr | DE041: ATM04201 | DE070: 301",
            respMti = "0810",
            respSummary = "ISO-8583 Echo Response (DE39=00 | Host Latency 14ms)",
            respCode = "00 (ECHO_OK)",
            respHex = "30 38 31 30 82 20 00 00 02 00 00 00 04 00 00 00\n" +
                "DE011: $stanStr | DE039: 00 | DE041: ATM04201 | DE070: 301"
        )
        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                activeBannerAlert = "ISO-8583 0800/0810 Echo Handshake Completed (STAN #$stanStr)"
            )
        }
    }

    fun sendDukptKeyExchange() {
        val nextStan = _uiState.value.stanCounter + 1
        val stanStr = nextStan.toString()
        appendNdcIsoPair(
            stan = stanStr,
            reqMti = "0800",
            reqSummary = "TR-31 / ANSI X9.24 DUKPT Working Key Rotation Request (DE70=161)",
            reqHex = "30 38 30 30 82 20 00 00 00 00 00 00 04 00 00 04\n" +
                "DE011: $stanStr | DE041: ATM04201 | DE070: 161 (DYNAMIC_KEY_EXCHANGE)\n" +
                "EPP Serial: PCI6-EPP-004291",
            respMti = "0810",
            respSummary = "Host TR-31 Key Block Loaded into EPP Cryptoprocessor (DE39=00)",
            respCode = "00 (KEY_LOADED)",
            respHex = "30 38 31 30 82 20 00 00 02 00 00 00 04 00 00 04\n" +
                "DE011: $stanStr | DE039: 00 | DE123: TR31_B0112P0TE00N0000_KCV_8F2A"
        )
        _uiState.update {
            it.copy(
                stanCounter = nextStan,
                activeBannerAlert = "EPP TR-31 Cryptogram & DUKPT Working Key Rotated (KCV 8F2A)"
            )
        }
    }

    fun dismissBannerAlert() {
        _uiState.update { it.copy(activeBannerAlert = null) }
    }

    private fun appendXfsEvent(
        mti: String,
        summary: String,
        rawDetail: String,
        isError: Boolean = false
    ) {
        val msg = NdcIsoMessage(
            id = "XFS-${System.nanoTime()}",
            timestamp = nowTime(),
            direction = "XFS-EVENT",
            protocol = "CEN/XFS 3.30",
            mti = mti,
            summary = summary,
            stan = _uiState.value.stanCounter.toString(),
            de39ResponseCode = if (isError) "WFS_ERR_HARDWARE" else "WFS_SUCCESS (0)",
            rawHexAscii = rawDetail,
            isError = isError
        )
        _uiState.update { state ->
            val updatedList = listOf(msg) + state.ndcMessages.take(35)
            state.copy(
                ndcMessages = updatedList,
                selectedNdcMessageId = msg.id
            )
        }
    }

    private fun appendNdcIsoPair(
        stan: String,
        reqMti: String,
        reqSummary: String,
        reqHex: String,
        respMti: String,
        respSummary: String,
        respCode: String,
        respHex: String,
        isError: Boolean = false
    ) {
        val reqMsg = NdcIsoMessage(
            id = "TX-${System.nanoTime()}",
            timestamp = nowTime(),
            direction = "TX -> HOST",
            protocol = "NDC+ / ISO-8583",
            mti = reqMti,
            summary = reqSummary,
            stan = stan,
            de39ResponseCode = "--",
            rawHexAscii = reqHex,
            isError = false
        )
        val respMsg = NdcIsoMessage(
            id = "RX-${System.nanoTime() + 1}",
            timestamp = nowTime(),
            direction = "RX <- HOST",
            protocol = "NDC+ / ISO-8583",
            mti = respMti,
            summary = respSummary,
            stan = stan,
            de39ResponseCode = respCode,
            rawHexAscii = respHex,
            isError = isError
        )
        _uiState.update { state ->
            val updatedList = listOf(respMsg, reqMsg) + state.ndcMessages.take(34)
            state.copy(
                ndcMessages = updatedList,
                selectedNdcMessageId = respMsg.id
            )
        }
    }
}
