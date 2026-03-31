package com.verihubs.nfcreader.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verihubs.nfcreader.data.model.*
import com.verihubs.nfcreader.nfc.NFCViewModel
import com.verihubs.nfcreader.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: NFCViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history = uiState.scanHistory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Riwayat Scan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "${history.size} hasil scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            if (history.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = SurfaceDark) {
                    Text(
                        "Semua",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryBlue
                    )
                }
            }
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada riwayat scan", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Hasil scan akan muncul di sini", style = MaterialTheme.typography.bodyMedium, color = TextSecondary.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    HistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: ScanHistoryItem) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val dateStr = dateFormat.format(Date(item.scannedAt))

    val typeIcon = when (item.scanType) {
        ScanType.E_CERTIFICATE -> Icons.Filled.WorkspacePremium
        ScanType.E_KTP -> Icons.Filled.ContactCard
        ScanType.UNKNOWN -> Icons.Filled.Help
    }
    val typeColor = when (item.scanType) {
        ScanType.E_CERTIFICATE -> PrimaryBlue
        ScanType.E_KTP -> SecondaryTeal
        ScanType.UNKNOWN -> TextSecondary
    }
    val typeLabel = when (item.scanType) {
        ScanType.E_CERTIFICATE -> "E-Sertifikat"
        ScanType.E_KTP -> "E-KTP"
        ScanType.UNKNOWN -> "Tidak diketahui"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = typeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.holderName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = typeColor)
                Text(item.documentId, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(alpha = 0.6f))
            }

            // Verification badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (item.isVerified) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (item.isVerified) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (item.isVerified) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (item.isVerified) "Valid" else "Invalid",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isVerified) SuccessGreen else ErrorRed
                    )
                }
            }
        }
    }
}
