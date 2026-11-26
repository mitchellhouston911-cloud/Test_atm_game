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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Terminal
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
import com.example.model.NdcIsoMessage
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
fun NdcIsoTraceView(
    uiState: AtmTerminalUiState,
    onSelectMessage: (String) -> Unit,
    onSendEchoTest: () -> Unit,
    onSendDukptKeyRotate: () -> Unit
) {
    val selectedMessage = uiState.ndcMessages.find { it.id == uiState.selectedNdcMessageId }
        ?: uiState.ndcMessages.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Host Link Control Bar
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
                        text = "NDC+ / ISO-8583 HOST AUTHORIZATION & XFS IPC TRACE",
                        color = AccentCyanLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "TLS 1.3 Host Socket 10.24.88.14:8583 • STAN #${uiState.stanCounter}",
                        color = TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendEchoTest,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("ndc_send_echo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TX 0800 ECHO TEST",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onSendDukptKeyRotate,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("ndc_rotate_dukpt_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ROTATE TR-31 KEY",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Selected Packet Hex / Data Element Decoder Inspector
        selectedMessage?.let { msg ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF041322))
                    .border(1.5.dp, AccentCyanLight, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Hex Packet Decoder",
                            tint = AccentCyanLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PACKET INSPECTOR [${msg.protocol} • ${msg.mti}]",
                            color = AccentCyanLight,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "DE39: ${msg.de39ResponseCode}",
                        color = if (msg.isError) AccentRedLight else AccentGreenLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = msg.summary,
                    color = TextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF020813))
                        .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = msg.rawHexAscii,
                        color = AccentGreenLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Chronological NDC+ / ISO-8583 / CEN-XFS Message Stream
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.ndcMessages.forEach { msg ->
                NdcTraceRow(
                    msg = msg,
                    isSelected = msg.id == selectedMessage?.id,
                    onClick = { onSelectMessage(msg.id) }
                )
            }
        }
    }
}

@Composable
private fun NdcTraceRow(
    msg: NdcIsoMessage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val badgeColor = when {
        msg.isError -> AccentRed
        msg.direction.startsWith("TX") -> AccentBlue
        msg.direction.startsWith("RX") -> AccentGreen
        else -> AccentAmber
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF172554) else BgCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentCyanLight else BorderSteel,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("ndc_msg_${msg.id}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${msg.direction} • ${msg.mti}",
                        color = badgeColor,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "STAN #${msg.stan}",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }

            Text(
                text = msg.timestamp,
                color = TextMuted,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp
            )
        }

        Text(
            text = msg.summary,
            color = TextMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
