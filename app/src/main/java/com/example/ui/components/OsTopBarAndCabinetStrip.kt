package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AtmTerminalUiState
import com.example.model.ConsoleTab
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
import com.example.ui.theme.CabinetStripBg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.OsBadgeBg
import com.example.ui.theme.OsBadgeText
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted

@Composable
fun OsTopBarAndCabinetStrip(
    uiState: AtmTerminalUiState,
    onSelectTab: (ConsoleTab) -> Unit,
    onToggleSupervisorKey: () -> Unit,
    onToggleSafeDoor: () -> Unit,
    onDismissBanner: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Windows 10 IoT Enterprise LTSC Kiosk OS Status Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = stringResource(R.string.terminal_id_badge),
                    bgColor = OsBadgeBg,
                    textColor = OsBadgeText,
                    borderColor = Color(0xFF3B82F6),
                    showDot = false
                )

                val isFault = uiState.hasCriticalHardwareFault
                val isMaint = uiState.supervisorMaintMode
                when {
                    isFault -> StatusBadge(
                        text = stringResource(R.string.ndc_fault_badge),
                        bgColor = AccentRed.copy(alpha = 0.18f),
                        textColor = AccentRedLight,
                        borderColor = AccentRed.copy(alpha = 0.5f),
                        showDot = true
                    )
                    isMaint -> StatusBadge(
                        text = stringResource(R.string.ndc_maint_badge),
                        bgColor = AccentAmber.copy(alpha = 0.18f),
                        textColor = AccentAmberLight,
                        borderColor = AccentAmber.copy(alpha = 0.5f),
                        showDot = true
                    )
                    else -> StatusBadge(
                        text = stringResource(R.string.ndc_online_badge),
                        bgColor = AccentGreen.copy(alpha = 0.16f),
                        textColor = AccentGreenLight,
                        borderColor = AccentGreen.copy(alpha = 0.45f),
                        showDot = true
                    )
                }

                StatusBadge(
                    text = stringResource(R.string.xfs_version_badge),
                    bgColor = AccentBlue.copy(alpha = 0.16f),
                    textColor = AccentCyanLight,
                    borderColor = AccentBlue.copy(alpha = 0.45f),
                    showDot = false
                )

                StatusBadge(
                    text = "VAULT: $${String.format("%,d", uiState.totalVaultCashUsd)}",
                    bgColor = BgCard,
                    textColor = TextMain,
                    borderColor = BorderSteel,
                    showDot = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4 Console View Tabs matching the HTML navigation tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConsoleTab.entries.forEach { tab ->
                    val isSelected = uiState.activeTab == tab
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF0284C7) else BgCard
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AccentCyanLight else BorderSteel,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom border of OS Topbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSteel)
        )

        // 2. Physical Supervisor Key Switch & CEN Safe Door Telemetry Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CabinetStripBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rotating Supervisor Key Cylinder Switch
            val keyRotation by animateFloatAsState(
                targetValue = if (uiState.supervisorMaintMode) 90f else 0f,
                label = "key_cylinder_rotation"
            )
            val keyBorderColor = if (uiState.supervisorMaintMode) AccentAmber else Color(0xFF475569)
            val keyTextColor = if (uiState.supervisorMaintMode) AccentAmberLight else Color(0xFFE2E8F0)

            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, keyBorderColor, RoundedCornerShape(999.dp))
                    .clickable { onToggleSupervisorKey() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("supervisor_key_switch"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cylinder visual
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(keyRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFCBD5E1), Color(0xFF64748B))
                            )
                        )
                        .border(2.dp, keyBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(11.dp)
                            .background(Color(0xFF0F172A))
                    )
                }

                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Supervisor Key",
                    tint = keyTextColor,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = if (uiState.supervisorMaintMode) {
                        stringResource(R.string.supervisor_key_maint)
                    } else {
                        stringResource(R.string.supervisor_key_normal)
                    },
                    color = keyTextColor,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // CEN L1 Safe Door Lock Indicator
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        1.dp,
                        if (uiState.safeDoorLocked) BorderSteel else AccentAmber,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleSafeDoor() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("safe_door_toggle_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (uiState.safeDoorLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Safe Door Status",
                    tint = if (uiState.safeDoorLocked) AccentGreenLight else AccentAmberLight,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (uiState.safeDoorLocked) "SAFE: BOLTED" else "SAFE: OPEN",
                    color = if (uiState.safeDoorLocked) AccentGreenLight else AccentAmberLight,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Compliance & Training Notice Sub-strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090E1A))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = AccentCyanLight,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = stringResource(R.string.compliance_notice),
                    color = TextMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "STAN #${uiState.stanCounter}",
                color = AccentCyanLight,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Optional Live Telemetry Alert Banner
        uiState.activeBannerAlert?.let { bannerText ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .border(1.dp, AccentCyanLight.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "XFS Telemetry Alert",
                        tint = AccentCyanLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = bannerText,
                        color = TextMain,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onDismissBanner,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("dismiss_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss alert",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    showDot: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
        }
        Text(
            text = text,
            color = textColor,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
