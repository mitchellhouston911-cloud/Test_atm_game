package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AtmTerminalUiState
import com.example.model.XfsDeviceHealth
import com.example.model.XfsServiceProvider
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
fun XfsTelemetryView(
    uiState: AtmTerminalUiState,
    onExecuteXfsCommand: (String, String) -> Unit,
    onInjectFault: (String, String, String) -> Unit,
    onClearAllFaults: () -> Unit,
    onRunSelfTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hardware Cutaway Banner + Global XFS Manager Controls
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
                        text = "CEN/XFS 3.30 SERVICE PROVIDER MANAGER (WOSA/XFS)",
                        color = AccentCyanLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "msxfs.dll v3.30.0018 • 5 Active Logical Device Providers",
                        color = TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(
                    text = if (uiState.hasCriticalHardwareFault) "WFS_SYSE_HWERROR" else "5/5 SP ONLINE",
                    bgColor = if (uiState.hasCriticalHardwareFault) {
                        AccentRed.copy(alpha = 0.18f)
                    } else {
                        AccentGreen.copy(alpha = 0.18f)
                    },
                    textColor = if (uiState.hasCriticalHardwareFault) AccentRedLight else AccentGreenLight,
                    borderColor = if (uiState.hasCriticalHardwareFault) AccentRed else AccentGreen,
                    showDot = true
                )
            }

            Image(
                painter = painterResource(id = R.drawable.img_atm_chassis_banner),
                contentDescription = "ATM CEN/XFS Hardware Telemetry Cutaway Schematic",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSteel, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunSelfTest,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("xfs_run_self_test_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WFSGetInfo() SELF-TEST",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onClearAllFaults,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("xfs_reset_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WFS_CMD_RESET ALL",
                        color = Color(0xFF0F172A),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Individual Service Provider Telemetry Cards (CDM, IDC, PIN, PTR, SIU)
        uiState.serviceProviders.forEach { sp ->
            XfsServiceProviderCard(
                sp = sp,
                onExecuteCommand = { cmd -> onExecuteXfsCommand(sp.classCode, cmd) },
                onInjectFault = { faultCode, desc -> onInjectFault(sp.classCode, faultCode, desc) }
            )
        }
    }
}

@Composable
private fun XfsServiceProviderCard(
    sp: XfsServiceProvider,
    onExecuteCommand: (String) -> Unit,
    onInjectFault: (String, String) -> Unit
) {
    val borderColor = when (sp.health) {
        XfsDeviceHealth.ONLINE -> BorderSteel
        XfsDeviceHealth.WARN -> AccentAmber
        XfsDeviceHealth.FAULT -> AccentRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
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
                        text = "${sp.classCode}310",
                        color = AccentCyanLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = sp.logicalName,
                        color = TextMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sp.hardwareModel} • ${sp.firmwareVersion}",
                        color = TextMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when (sp.health) {
                        XfsDeviceHealth.ONLINE -> Icons.Default.CheckCircle
                        XfsDeviceHealth.WARN -> Icons.Default.Warning
                        XfsDeviceHealth.FAULT -> Icons.Default.Error
                    },
                    contentDescription = sp.health.label,
                    tint = when (sp.health) {
                        XfsDeviceHealth.ONLINE -> AccentGreenLight
                        XfsDeviceHealth.WARN -> AccentAmberLight
                        XfsDeviceHealth.FAULT -> AccentRedLight
                    },
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = sp.health.xfsCode,
                    color = when (sp.health) {
                        XfsDeviceHealth.ONLINE -> AccentGreenLight
                        XfsDeviceHealth.WARN -> AccentAmberLight
                        XfsDeviceHealth.FAULT -> AccentRedLight
                    },
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Status register line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = sp.statusDetail,
                    color = if (sp.health == XfsDeviceHealth.FAULT) AccentRedLight else AccentGreenLight,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sp.extraMetricLabel}: ${sp.extraMetricValue}",
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
        }

        // Direct WFSAsyncExecute Command Buttons & Fault Injection Trigger
        val (primaryCmd, faultCode, faultDesc) = when (sp.classCode) {
            "CDM" -> Triple(
                "WFS_CMD_CDM_RESET",
                "WFS_ERR_CDM_SHUTTERJAMMED",
                "Optical beam blocked at presenter throat"
            )
            "IDC" -> Triple(
                "WFS_CMD_IDC_RESET",
                "WFS_ERR_IDC_MEDIAJAM",
                "Card transport roller slip detected"
            )
            "PIN" -> Triple(
                "WFS_CMD_PIN_INITIALIZATION",
                "WFS_ERR_PIN_KEYNOTFOUND",
                "DUKPT IPEK key slot parity mismatch"
            )
            "PTR" -> Triple(
                "WFS_CMD_PTR_CONTROL_MEDIA",
                "WFS_ERR_PTR_PAPEROUT",
                "Thermal supply sensor low/out"
            )
            else -> Triple(
                "WFS_CMD_SIU_RESET",
                "WFS_ERR_SIU_PORTERROR",
                "Cabinet seismic shock sensor triggered"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, AccentCyanLight, RoundedCornerShape(8.dp))
                    .clickable { onExecuteCommand(primaryCmd) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("sp_cmd_${sp.classCode.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = AccentCyanLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = primaryCmd,
                        color = AccentCyanLight,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(alpha = 0.15f))
                    .border(1.dp, AccentRed, RoundedCornerShape(8.dp))
                    .clickable { onInjectFault(faultCode, faultDesc) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("sp_fault_${sp.classCode.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SIMULATE FAULT",
                    color = AccentRedLight,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
