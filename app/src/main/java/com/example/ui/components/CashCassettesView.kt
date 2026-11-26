package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AtmTerminalUiState
import com.example.model.CashCassette
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyanLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgPanel
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted

@Composable
fun CashCassettesView(
    uiState: AtmTerminalUiState,
    onReplenishSingle: (String) -> Unit,
    onReplenishAll: () -> Unit,
    onTestPickReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vault Summary & Exchange Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgPanel)
                .border(1.dp, BorderSteel, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CEN L1 VAULT & WFS_INF_CDM_CASH_UNIT_INFO",
                        color = AccentCyanLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "4 Physical Recycling/Dispense Cassettes + Divert/Reject Bin",
                        color = TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,d", uiState.totalVaultCashUsd)}.00",
                        color = AccentGreenLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "TOTAL VAULT CASH",
                        color = TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReplenishAll,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("cassette_replenish_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FILL ALL (1,600 NOTES)",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onTestPickReject,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("cassette_test_pick_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEST PICK TO REJECT",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4 Physical Cassettes (CAS-1..CAS-4)
        uiState.cassettes.forEach { cassette ->
            CassetteInventoryCard(
                cassette = cassette,
                onReplenish = { onReplenishSingle(cassette.slotId) }
            )
        }

        // Dual Compartment Reject & Retract Bin Telemetry Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard)
                .border(1.dp, BorderSteel, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Reject and Retract Bin",
                    tint = AccentAmberLight,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "REJECT-BIN & RETRACT-VAULT (WFS_CDM_TYPEREJECTCASSETTE)",
                        color = TextMain,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Double-pick skew notes & customer timeout retracts",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "REJECT: ${uiState.rejectBinNotes} NOTES",
                    color = AccentAmberLight,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "PURGE: ${uiState.purgeBinNotes} NOTES",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CassetteInventoryCard(
    cassette: CashCassette,
    onReplenish: () -> Unit
) {
    val barColor = when {
        cassette.currentNotes == 0 -> AccentRed
        cassette.currentNotes < 250 -> AccentAmber
        else -> AccentGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, AccentCyanLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${cassette.slotId} ($${cassette.denominationUsd})",
                        color = AccentCyanLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = "USD $${cassette.denominationUsd} Denomination Hopper",
                        color = TextMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${cassette.statusLabel} • Diverted: ${cassette.divertedCount}",
                        color = if (cassette.currentNotes < 250) AccentAmberLight else TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,d", cassette.totalValueUsd)}",
                    color = TextMain,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${cassette.currentNotes} / ${cassette.maxCapacity} notes",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
        }

        // Fill Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(cassette.fillFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CAPACITY: ${(cassette.fillFraction * 100).toInt()}% • MAGNETIC / UV SENSOR READY",
                color = TextMuted,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp
            )

            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, AccentGreenLight, RoundedCornerShape(8.dp))
                    .clickable { onReplenish() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("replenish_${cassette.slotId.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+500 NOTES",
                    color = AccentGreenLight,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
