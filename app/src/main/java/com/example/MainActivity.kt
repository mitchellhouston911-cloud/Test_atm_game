package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConsoleTab
import com.example.ui.components.CashCassettesView
import com.example.ui.components.CustomerFasciaView
import com.example.ui.components.NdcIsoTraceView
import com.example.ui.components.OsTopBarAndCabinetStrip
import com.example.ui.components.XfsTelemetryView
import com.example.ui.theme.BgDesktop
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AtmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                XfsAtmConsoleApp()
            }
        }
    }
}

@Composable
fun XfsAtmConsoleApp(
    atmViewModel: AtmViewModel = viewModel()
) {
    val uiState by atmViewModel.uiState.collectAsStateWithLifecycle()

    // Ensure hardware back navigation returns from secondary console tabs to Customer Bezel
    if (uiState.activeTab != ConsoleTab.CUSTOMER_BEZEL) {
        BackHandler {
            atmViewModel.navigateBackToFascia()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDesktop),
        containerColor = BgDesktop,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDesktop)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 960.dp)
            ) {
                OsTopBarAndCabinetStrip(
                    uiState = uiState,
                    onSelectTab = atmViewModel::selectTab,
                    onToggleSupervisorKey = atmViewModel::toggleSupervisorKey,
                    onToggleSafeDoor = atmViewModel::toggleSafeDoorLock,
                    onDismissBanner = atmViewModel::dismissBannerAlert
                )

                AnimatedContent(
                    targetState = uiState.activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "console_tab_content",
                    modifier = Modifier.weight(1f)
                ) { currentTab ->
                    when (currentTab) {
                        ConsoleTab.CUSTOMER_BEZEL -> {
                            CustomerFasciaView(
                                uiState = uiState,
                                onFdkPressed = atmViewModel::onFdkPressed,
                                onEppDigit = atmViewModel::onEppDigitPressed,
                                onEppClear = atmViewModel::onEppClearPressed,
                                onEppCancel = atmViewModel::onEppCancelPressed,
                                onEppEnter = atmViewModel::onEppEnterPressed,
                                onInsertCard = atmViewModel::insertEmvCard,
                                onEjectCard = atmViewModel::ejectCard,
                                onSelectCardProfile = atmViewModel::selectTestCardProfile,
                                onCollectShutterItems = atmViewModel::collectShutterCashAndCard
                            )
                        }
                        ConsoleTab.XFS_TELEMETRY -> {
                            XfsTelemetryView(
                                uiState = uiState,
                                onExecuteXfsCommand = atmViewModel::executeXfsCommand,
                                onInjectFault = atmViewModel::injectHardwareFault,
                                onClearAllFaults = atmViewModel::clearAllFaults,
                                onRunSelfTest = atmViewModel::runDiagnosticSelfTest
                            )
                        }
                        ConsoleTab.CASH_CASSETTES -> {
                            CashCassettesView(
                                uiState = uiState,
                                onReplenishSingle = atmViewModel::replenishSingleCassette,
                                onReplenishAll = atmViewModel::replenishAllCassettes,
                                onTestPickReject = atmViewModel::testPickToRejectBin
                            )
                        }
                        ConsoleTab.NDC_TRACE -> {
                            NdcIsoTraceView(
                                uiState = uiState,
                                onSelectMessage = atmViewModel::selectNdcMessage,
                                onSendEchoTest = atmViewModel::sendIsoEchoTest,
                                onSendDukptKeyRotate = atmViewModel::sendDukptKeyExchange
                            )
                        }
                    }
                }
            }
        }
    }
}
