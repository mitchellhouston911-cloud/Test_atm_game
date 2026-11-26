package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Eject
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AtmScreenState
import com.example.model.AtmTerminalUiState
import com.example.model.DEFAULT_TEST_CARDS
import com.example.model.TestCardProfile
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyanLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BgCard
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CrtBg
import com.example.ui.theme.CrtBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted

@Composable
fun CustomerFasciaView(
    uiState: AtmTerminalUiState,
    onFdkPressed: (String) -> Unit,
    onEppDigit: (String) -> Unit,
    onEppClear: () -> Unit,
    onEppCancel: () -> Unit,
    onEppEnter: () -> Unit,
    onInsertCard: () -> Unit,
    onEjectCard: () -> Unit,
    onSelectCardProfile: (TestCardProfile) -> Unit,
    onCollectShutterItems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ATM Physical Steel Chassis Frame
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(3.dp, Color(0xFF475569), RoundedCornerShape(18.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fascia Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Terminal Fascia",
                        tint = AccentCyanLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "APEX CEN/XFS SELF-SERVICE TERMINAL",
                            color = AccentCyanLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "NCR / DIEBOLD NDC+ EMULATION BEZEL • PCI-PTS 6.x",
                            color = TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                // Privacy Filter & Anti-Skim LED Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.hasCriticalHardwareFault) AccentRedLight else AccentGreenLight
                            )
                    )
                    Text(
                        text = if (uiState.hasCriticalHardwareFault) "XFS FAULT" else "ANTI-SKIM OK",
                        color = if (uiState.hasCriticalHardwareFault) AccentRedLight else AccentGreenLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Screen Assembly: Left 4 FDK Softkeys + Center 15" Monitor + Right 4 FDK Softkeys
            val fdkLabels = getFdkMappingForState(uiState)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0B1220))
                    .border(2.dp, BorderSteel, RoundedCornerShape(14.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left FDK Column (L1..L4)
                Column(
                    modifier = Modifier.width(50.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    listOf("L1", "L2", "L3", "L4").forEach { keyId ->
                        FdkHardwareButton(
                            keyId = keyId,
                            arrowText = "$keyId ▶",
                            isEnabled = fdkLabels[keyId]?.isNotEmpty() == true,
                            onClick = { onFdkPressed(keyId) }
                        )
                    }
                }

                // Center CRT / LCD Polarized Display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CrtBg)
                        .border(2.dp, CrtBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    AtmCrtMonitorScreen(
                        uiState = uiState,
                        fdkLabels = fdkLabels
                    )
                }

                // Right FDK Column (R1..R4)
                Column(
                    modifier = Modifier.width(50.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    listOf("R1", "R2", "R3", "R4").forEach { keyId ->
                        FdkHardwareButton(
                            keyId = keyId,
                            arrowText = "◀ $keyId",
                            isEnabled = fdkLabels[keyId]?.isNotEmpty() == true,
                            onClick = { onFdkPressed(keyId) }
                        )
                    }
                }
            }

            // Lower Fascia Hardware Modules: EPP PIN Pad + Card Reader (IDC) + Cash Shutter (CDM) + Receipt (PTR)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // EMV Card Reader (IDC310) & Test Profile Selector Card
                IdcCardReaderBezelModule(
                    uiState = uiState,
                    onInsertCard = onInsertCard,
                    onEjectCard = onEjectCard,
                    onSelectCardProfile = onSelectCardProfile
                )

                // Encrypted PIN Pad (EPP / PIN310)
                EppPinPadHardwareModule(
                    onDigit = onEppDigit,
                    onClear = onEppClear,
                    onCancel = onEppCancel,
                    onEnter = onEppEnter
                )

                // Cash Dispenser Shutter (CDM310) & 80mm Thermal Receipt Presenter (PTR310)
                CdmCashShutterAndPtrReceiptModule(
                    uiState = uiState,
                    onCollectShutterItems = onCollectShutterItems
                )
            }
        }
    }
}

@Composable
private fun FdkHardwareButton(
    keyId: String,
    arrowText: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .width(50.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    if (isEnabled) {
                        listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
                    } else {
                        listOf(Color(0xFF475569), Color(0xFF334155))
                    }
                )
            )
            .border(
                width = 2.dp,
                color = if (isEnabled) Color(0xFF0EA5E9) else Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .testTag("fdk_button_${keyId.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = arrowText,
            color = if (isEnabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AtmCrtMonitorScreen(
    uiState: AtmTerminalUiState,
    fdkLabels: Map<String, String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Screen Status Header inside CRT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NDC+ SCREEN [${uiState.screenState.name}]",
                color = AccentCyanLight,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = uiState.selectedAccountType,
                color = AccentGreenLight,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Screen Dynamic Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071C30))
                .border(1.dp, Color(0xFF153B5C), RoundedCornerShape(8.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.screenState) {
                AtmScreenState.IDLE_WELCOME -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "WELCOME TO APEX CEN/XFS ATM",
                            color = TextMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "INSERT EMV TEST CARD IN IDC SLOT BELOW OR PRESS FDK R4",
                            color = AccentCyanLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Active Profile: ${uiState.selectedCard.maskedPan} (Test Bal: $${uiState.selectedCard.mockLedgerBalance})",
                            color = TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.CARD_READ_EMV -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "WFS_CMD_IDC_READ_RAW_DATA...",
                            color = AccentAmberLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Powering EMV Contact Chip • AID ${uiState.selectedCard.aid}",
                            color = TextMain,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.PIN_ENTRY -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ENTER 4-DIGIT PIN ON EPP KEYPAD",
                            color = TextMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val maskedPin = "*".repeat(uiState.enteredPin.length)
                            .padEnd(4, '•')
                            .toCharArray()
                            .joinToString(" ")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF020B14))
                                .border(1.dp, AccentCyanLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = maskedPin,
                                color = AccentGreenLight,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = uiState.pinErrorText ?: "Press ENTER (OK) on EPP or FDK R4 when done",
                            color = if (uiState.pinErrorText != null) AccentRedLight else TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.MAIN_MENU -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SELECT TRANSACTION (FDK L1-L4 / R1-R4)",
                            color = AccentCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Card: ${uiState.selectedCard.maskedPan} • Ledger: $${uiState.selectedCard.mockLedgerBalance}",
                            color = TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                AtmScreenState.FAST_CASH_SELECT -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SELECT FAST CASH AMOUNT (USD)",
                            color = AccentGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.cashErrorText?.let { err ->
                            Text(
                                text = err,
                                color = AccentRedLight,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                AtmScreenState.CUSTOM_AMOUNT_ENTRY -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ENTER WITHDRAWAL AMOUNT ($10 MULTIPLES)",
                            color = TextMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val displayAmt = if (uiState.customAmountBuffer.isEmpty()) "$0" else "$${uiState.customAmountBuffer}"
                        Text(
                            text = displayAmt,
                            color = AccentGreenLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = uiState.cashErrorText ?: "Use EPP Keypad + ENTER or Quick FDK Presets",
                            color = if (uiState.cashErrorText != null) AccentRedLight else TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.BALANCE_INQUIRY_RESULT -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ISO-8583 0210 LEDGER BALANCE APPROVED",
                            color = AccentGreenLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AVAILABLE: $${String.format("%,d", uiState.selectedCard.mockLedgerBalance)}.00",
                            color = TextMain,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "80mm Thermal Receipt Printed to PTR Slot",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                AtmScreenState.HOST_AUTH_TRANSMIT -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TRANSMITTING NDC+ / ISO-8583 0200...",
                            color = AccentCyanLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verifying TR-31 DUKPT PIN Block & Cassette Mix",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                AtmScreenState.DISPENSING_CASH -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "WFS_CMD_CDM_DISPENSE IN PROGRESS...",
                            color = AccentAmberLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vacuum Pick Module transporting notes to shutter",
                            color = TextMain,
                            fontSize = 11.sp
                        )
                    }
                }

                AtmScreenState.COLLECT_CASH_AND_CARD -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SHUTTER OPEN — PLEASE TAKE CASH ($${uiState.lastDispensedAmount})",
                            color = AccentGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        val mixText = uiState.lastDispenseBreakdown.joinToString(" + ") {
                            "${it.noteCount}x$${it.denomination} (${it.slotId})"
                        }
                        Text(
                            text = "Mix: $mixText",
                            color = AccentCyanLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.TRANSACTION_COMPLETE -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TRANSACTION COMPLETE — THANK YOU",
                            color = AccentGreenLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AtmScreenState.OUT_OF_SERVICE_FAULT -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Fault",
                            tint = AccentRedLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "TERMINAL TEMPORARILY OUT OF SERVICE",
                            color = AccentRedLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        val faultSummary = uiState.serviceProviders
                            .filter { it.activeFaultCode != null }
                            .joinToString(" | ") { "${it.classCode}: ${it.activeFaultCode}" }
                        Text(
                            text = faultSummary.ifEmpty { "CEN/XFS Hardware Error Active" },
                            color = AccentAmberLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AtmScreenState.SUPERVISOR_SOP_MODE -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CEN/XFS VENDOR DEPENDENT MODE (VDM / SOP)",
                            color = AccentAmberLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Customer Fascia Locked • Supervisor Key Active",
                            color = TextMain,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4 Rows of Left / Right FDK Screen Callouts aligned with L1..L4 and R1..R4
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (idx in 1..4) {
                val leftKey = "L$idx"
                val rightKey = "R$idx"
                val leftLabel = fdkLabels[leftKey].orEmpty()
                val rightLabel = fdkLabels[rightKey].orEmpty()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left FDK label box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (leftLabel.isNotEmpty()) Color(0xFF0C2742) else Color(0xFF04101C)
                            )
                            .border(
                                1.dp,
                                if (leftLabel.isNotEmpty()) Color(0xFF0EA5E9).copy(alpha = 0.5f) else Color(0xFF0B2136),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (leftLabel.isNotEmpty()) "◀ $leftLabel" else "",
                            color = TextMain,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right FDK label box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (rightLabel.isNotEmpty()) Color(0xFF0C2742) else Color(0xFF04101C)
                            )
                            .border(
                                1.dp,
                                if (rightLabel.isNotEmpty()) Color(0xFF0EA5E9).copy(alpha = 0.5f) else Color(0xFF0B2136),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = if (rightLabel.isNotEmpty()) "$rightLabel ▶" else "",
                            color = TextMain,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun getFdkMappingForState(uiState: AtmTerminalUiState): Map<String, String> {
    if (uiState.supervisorMaintMode) {
        return mapOf(
            "L1" to "RUN XFS SELF-TEST",
            "L2" to "FILL CASSETTES",
            "L3" to "",
            "L4" to "",
            "R1" to "CLEAR ALL FAULTS",
            "R2" to "",
            "R3" to "",
            "R4" to "EXIT SOP MODE"
        )
    }
    return when (uiState.screenState) {
        AtmScreenState.IDLE_WELCOME -> mapOf(
            "L4" to "INSERT EMV CARD",
            "R4" to "INSERT EMV CARD"
        )
        AtmScreenState.PIN_ENTRY -> mapOf(
            "L4" to "CANCEL / EJECT",
            "R4" to "SUBMIT PIN"
        )
        AtmScreenState.MAIN_MENU -> mapOf(
            "L1" to "FAST CASH MENU",
            "L2" to "CUSTOM AMOUNT",
            "L3" to "BALANCE INQUIRY",
            "L4" to "EJECT CARD",
            "R1" to "QUICK $100",
            "R2" to "QUICK $200",
            "R3" to "ACCT: ${uiState.selectedAccountType.take(8)}",
            "R4" to "PRINT RECEIPT"
        )
        AtmScreenState.FAST_CASH_SELECT -> mapOf(
            "L1" to "WITHDRAW $20",
            "L2" to "WITHDRAW $50",
            "L3" to "WITHDRAW $100",
            "L4" to "BACK TO MENU",
            "R1" to "WITHDRAW $200",
            "R2" to "WITHDRAW $300",
            "R3" to "WITHDRAW $500",
            "R4" to "OTHER AMOUNT"
        )
        AtmScreenState.CUSTOM_AMOUNT_ENTRY -> mapOf(
            "L1" to "PRESET $40",
            "L2" to "PRESET $80",
            "L3" to "PRESET $150",
            "L4" to "BACK TO MENU",
            "R3" to "CLEAR AMOUNT",
            "R4" to "CONFIRM DISPENSE"
        )
        AtmScreenState.BALANCE_INQUIRY_RESULT -> mapOf(
            "L1" to "FAST CASH MENU",
            "L4" to "MAIN MENU",
            "R4" to "EJECT CARD"
        )
        AtmScreenState.COLLECT_CASH_AND_CARD -> mapOf(
            "L4" to "TAKE CASH & CARD",
            "R4" to "TAKE CASH & CARD"
        )
        AtmScreenState.TRANSACTION_COMPLETE -> mapOf(
            "L1" to "ANOTHER TXN",
            "R4" to "EJECT CARD"
        )
        AtmScreenState.OUT_OF_SERVICE_FAULT -> mapOf(
            "L4" to "OPEN XFS DIAG",
            "R4" to "RESET XFS FAULTS"
        )
        else -> emptyMap()
    }
}

@Composable
private fun IdcCardReaderBezelModule(
    uiState: AtmTerminalUiState,
    onInsertCard: () -> Unit,
    onEjectCard: () -> Unit,
    onSelectCardProfile: (TestCardProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, BorderSteel, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Motorized IDC Card Reader",
                    tint = AccentCyanLight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "IDC310 MOTORIZED EMV CARD READER",
                    color = TextMain,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Illuminated Card Slot throat LED
            val ledColor = when {
                uiState.cardInReaderWaitingTake -> AccentAmberLight
                uiState.isCardInserted -> AccentCyanLight
                else -> AccentGreenLight
            }
            Text(
                text = when {
                    uiState.cardInReaderWaitingTake -> "LED: TAKE CARD"
                    uiState.isCardInserted -> "CHIP CLAMPED"
                    else -> "LED: READY INSERT"
                },
                color = ledColor,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Physical Card Reader Slot graphic + Insert/Eject Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Illuminated Card Slot Bezel Visual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF020617))
                    .border(
                        2.dp,
                        if (uiState.isCardInserted) AccentCyanLight else AccentGreen,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (uiState.isCardInserted) AccentCyanLight else Color(0xFF334155)
                        )
                )
            }

            if (!uiState.isCardInserted) {
                Button(
                    onClick = onInsertCard,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("idc_insert_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INSERT CARD",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onEjectCard,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("idc_eject_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Eject,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EJECT CARD",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Selectable EMV Test Card Profiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DEFAULT_TEST_CARDS.forEach { card ->
                val isSelected = uiState.selectedCard.id == card.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1E3A8A) else BgCard)
                        .border(
                            1.dp,
                            if (isSelected) AccentCyanLight else BorderSteel,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCardProfile(card) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("card_profile_${card.id.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.maskedPan.takeLast(9),
                            color = if (isSelected) Color.White else TextMain,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bal: $${card.mockLedgerBalance}",
                            color = if (isSelected) AccentCyanLight else TextMuted,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EppPinPadHardwareModule(
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    onEnter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, BorderSteel, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "EPP Encrypted PIN Pad",
                    tint = AccentGreenLight,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "PIN310 PCI-PTS 6.x ENCRYPTED PIN PAD (EPP)",
                    color = TextMain,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "TR-31 / DUKPT",
                color = AccentGreenLight,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 4x4 Industrial Brushed Steel EPP Layout
        val rows = listOf(
            listOf("1", "2", "3", "CANCEL"),
            listOf("4", "5", "6", "CLEAR"),
            listOf("7", "8", "9", "ENTER"),
            listOf("00", "0", ".", "OK")
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowKeys.forEach { keyLabel ->
                        val isActionKey = keyLabel in listOf("CANCEL", "CLEAR", "ENTER", "OK")
                        val keyBg = when (keyLabel) {
                            "CANCEL" -> AccentRed
                            "CLEAR" -> AccentAmber
                            "ENTER", "OK" -> AccentGreen
                            else -> Color(0xFF334155)
                        }
                        val keyTextColor = when (keyLabel) {
                            "CLEAR", "ENTER", "OK" -> Color(0xFF0F172A)
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .weight(if (isActionKey) 1.25f else 1f)
                                .minimumInteractiveComponentSize()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(keyBg)
                                .border(1.dp, Color(0xFF64748B), RoundedCornerShape(8.dp))
                                .clickable {
                                    when (keyLabel) {
                                        "CANCEL" -> onCancel()
                                        "CLEAR" -> onClear()
                                        "ENTER", "OK" -> onEnter()
                                        "." -> Unit
                                        else -> onDigit(keyLabel)
                                    }
                                }
                                .testTag("epp_key_${keyLabel.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keyLabel,
                                color = keyTextColor,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = if (isActionKey) 11.sp else 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CdmCashShutterAndPtrReceiptModule(
    uiState: AtmTerminalUiState,
    onCollectShutterItems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, BorderSteel, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // CDM310 Cash Presenter Shutter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = "Cash Dispenser Shutter",
                    tint = if (uiState.cashInShutterWaiting) AccentGreenLight else AccentCyanLight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "CDM310 CASH PRESENTER SHUTTER",
                    color = TextMain,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (uiState.cashInShutterWaiting) "WFS_CDM_SHTOPEN" else "WFS_CDM_SHTCLOSED",
                color = if (uiState.cashInShutterWaiting) AccentGreenLight else TextMuted,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.cashInShutterWaiting) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentGreen.copy(alpha = 0.16f))
                    .border(2.dp, AccentGreenLight, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DISPENSED BUNDLE READY: $${uiState.lastDispensedAmount}.00",
                        color = AccentGreenLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = uiState.lastDispenseBreakdown.joinToString(", ") {
                            "${it.noteCount}x$${it.denomination} (${it.slotId})"
                        },
                        color = TextMain,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp
                    )
                }
                Button(
                    onClick = onCollectShutterItems,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("cdm_collect_cash_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TAKE CASH",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF020617))
                    .border(1.dp, BorderSteel, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "STEEL PRESENTER SHUTTER CLOSED & LOCKED",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
        }

        // PTR310 80mm Thermal Receipt Presenter Output
        uiState.latestReceipt?.let { receipt ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Thermal Receipt",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "PTR310 80MM THERMAL RECEIPT • ${receipt.terminalId}",
                            color = Color(0xFF0F172A),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "STAN #${receipt.stan}",
                        color = Color(0xFF334155),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "TIME: ${receipt.timestamp} | PAN: ${receipt.maskedPan} | AID: ${receipt.aid}",
                    color = Color(0xFF334155),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp
                )
                Text(
                    text = "TXN: ${receipt.transactionType} | AUTH: ${receipt.authCode}",
                    color = Color(0xFF0F172A),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (receipt.dispensedAmount > 0) {
                    Text(
                        text = "DISPENSED: $${receipt.dispensedAmount}.00 | REMAINING TEST BAL: $${receipt.remainingLedger}.00",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = "LEDGER BALANCE: $${receipt.remainingLedger}.00",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
