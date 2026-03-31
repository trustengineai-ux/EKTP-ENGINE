package com.verihubs.nfcreader.ui.screens

import android.nfc.NfcAdapter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verihubs.nfcreader.data.model.*
import com.verihubs.nfcreader.nfc.*
import com.verihubs.nfcreader.ui.components.*
import com.verihubs.nfcreader.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    nfcAdapter: NfcAdapter?,
    viewModel: NFCViewModel,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNFCAvailable = nfcAdapter != null
    val isNFCEnabled = nfcAdapter?.isEnabled == true

    LaunchedEffect(Unit) {
        if (isNFCAvailable && isNFCEnabled) {
            viewModel.startWaiting()
        }
        viewModel.setNFCEnabled(isNFCEnabled)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF162032))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ScannerTopBar(
                scanMode = uiState.selectedScanMode,
                onModeChange = { viewModel.setScanMode(it) }
            )

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState.scanState) {
                    is NFCScanState.Idle, NFCScanState.WaitingForCard -> {
                        NFCScanIdleView(
                            isNFCAvailable = isNFCAvailable,
                            isNFCEnabled = isNFCEnabled,
                            scanMode = uiState.selectedScanMode
                        )
                    }
                    is NFCScanState.ReadingCard -> {
                        NFCReadingView()
                    }
                    is NFCScanState.Verifying -> {
                        NFCVerifyingView()
                    }
                    is NFCScanState.ECertificateVerified -> {
                        ECertificateResultView(
                            data = state.data,
                            onScanAgain = { viewModel.resetState(); viewModel.startWaiting() },
                            onViewHistory = onNavigateToHistory
                        )
                    }
                    is NFCScanState.EKTPVerified -> {
                        EKTPResultView(
                            data = state.data,
                            onScanAgain = { viewModel.resetState(); viewModel.startWaiting() },
                            onViewHistory = onNavigateToHistory
                        )
                    }
                    is NFCScanState.Error -> {
                        NFCScanErrorView(
                            message = state.message,
                            onRetry = { viewModel.resetState(); viewModel.startWaiting() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ScannerTopBar(
    scanMode: ScanMode,
    onModeChange: (ScanMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NFC Reader",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Powered by Verihubs",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryTeal
                )
            }
            Surface(
                shape = CircleShape,
                color = SurfaceDark,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Nfc,
                    contentDescription = "NFC",
                    tint = PrimaryBlue,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScanMode.entries.forEach { mode ->
                val selected = mode == scanMode
                FilterChip(
                    selected = selected,
                    onClick = { onModeChange(mode) },
                    label = {
                        Text(
                            text = when (mode) {
                                ScanMode.AUTO_DETECT -> "Auto"
                                ScanMode.E_CERTIFICATE -> "E-Sertifikat"
                                ScanMode.E_KTP -> "E-KTP"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = CardDark,
                        selectedBorderColor = PrimaryBlue
                    )
                )
            }
        }
    }
}

@Composable
fun NFCScanIdleView(
    isNFCAvailable: Boolean,
    isNFCEnabled: Boolean,
    scanMode: ScanMode
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by pulseAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isNFCAvailable) {
            NFCUnavailableCard()
        } else if (!isNFCEnabled) {
            NFCDisabledCard()
        } else {
            // Animated NFC scan indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // Outer pulse ring
                Canvas(modifier = Modifier.fillMaxSize().scale(scale).alpha(alpha)) {
                    drawCircle(
                        color = PrimaryBlue,
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
                // Middle ring
                Canvas(modifier = Modifier.fillMaxSize(0.7f).scale(scale).alpha(alpha * 0.6f)) {
                    drawCircle(
                        color = SecondaryTeal,
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f.dp.toPx())
                    )
                }
                // Center icon
                Surface(
                    shape = CircleShape,
                    color = SurfaceDark,
                    border = BorderStroke(2.dp, PrimaryBlue),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Nfc,
                            contentDescription = "NFC",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Siap Memindai",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (scanMode) {
                    ScanMode.AUTO_DETECT -> "Tempelkan e-KTP atau kartu sertifikat\nke bagian belakang perangkat"
                    ScanMode.E_CERTIFICATE -> "Tempelkan kartu e-Sertifikat\nke bagian belakang perangkat"
                    ScanMode.E_KTP -> "Tempelkan e-KTP\nke bagian belakang perangkat"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Hint card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Pastikan kartu NFC menempel rata dan tidak bergerak selama proses scan",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun NFCReadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text("Membaca chip NFC…", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Jangan pindahkan kartu", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun NFCVerifyingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = SecondaryTeal, modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text("Memverifikasi dengan Verihubs…", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Menghubungi server verifikasi", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun NFCUnavailableCard() {
    Surface(shape = RoundedCornerShape(16.dp), color = SurfaceDark) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.NfcRounded, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("NFC Tidak Tersedia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Perangkat ini tidak memiliki hardware NFC", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NFCDisabledCard() {
    Surface(shape = RoundedCornerShape(16.dp), color = SurfaceDark) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.WifiOff, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("NFC Tidak Aktif", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Aktifkan NFC di Pengaturan > Koneksi > NFC", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NFCScanErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = ErrorRed.copy(alpha = 0.15f), modifier = Modifier.size(100.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(52.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Gagal Memverifikasi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Coba Lagi", fontWeight = FontWeight.SemiBold)
        }
    }
}
