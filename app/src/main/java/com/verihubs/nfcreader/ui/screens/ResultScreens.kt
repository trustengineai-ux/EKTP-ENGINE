package com.verihubs.nfcreader.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.verihubs.nfcreader.data.model.*
import com.verihubs.nfcreader.ui.theme.*

// ============================================================
// E-CERTIFICATE RESULT SCREEN
// ============================================================

@Composable
fun ECertificateResultView(
    data: ECertificateData,
    onScanAgain: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Status banner
        VerificationStatusBanner(
            isValid = data.isValid,
            score = data.verificationScore
        )

        Spacer(Modifier.height(20.dp))

        // Certificate card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "E-Sertifikat",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            data.certificateType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardDark)

                // Fields
                ResultField(label = "Nama Pemegang", value = data.holderName, icon = Icons.Outlined.Person)
                ResultField(label = "NIK", value = data.holderNik ?: "-", icon = Icons.Outlined.Badge)
                ResultField(label = "Nomor Sertifikat", value = data.certificateNumber ?: "-", icon = Icons.Outlined.Tag)
                ResultField(label = "Diterbitkan Oleh", value = data.issuedBy, icon = Icons.Outlined.AccountBalance)
                ResultField(label = "Tanggal Terbit", value = data.issuedDate, icon = Icons.Outlined.CalendarToday)
                ResultField(
                    label = "Berlaku Hingga",
                    value = data.expiredDate ?: "Seumur hidup",
                    icon = Icons.Outlined.EventAvailable,
                    valueColor = if (data.expiredDate != null) WarningAmber else SuccessGreen
                )

                // Additional data
                if (!data.additionalData.isNullOrEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardDark)
                    Text("Data Tambahan", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    data.additionalData.forEach { (key, value) ->
                        ResultField(label = key, value = value, icon = Icons.Outlined.Info)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        ActionButtons(onScanAgain = onScanAgain, onViewHistory = onViewHistory)
    }
}

// ============================================================
// E-KTP RESULT SCREEN
// ============================================================

@Composable
fun EKTPResultView(
    data: EKTPData,
    onScanAgain: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Status banner
        VerificationStatusBanner(
            isValid = data.isChipValid,
            score = null,
            chipAuthPassed = data.chipAuthPassed
        )

        Spacer(Modifier.height(20.dp))

        // KTP card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SecondaryTeal.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.ContactCard,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Kartu Tanda Penduduk", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("E-KTP Elektronik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardDark)

                // Chip verification status badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipStatusBadge(label = "Chip Valid", isValid = data.isChipValid)
                    ChipStatusBadge(label = "Auth Chip", isValid = data.chipAuthPassed)
                }

                Spacer(Modifier.height(16.dp))

                // KTP Data Fields
                ResultField(label = "NIK", value = data.nik, icon = Icons.Outlined.Tag)
                ResultField(label = "Nama Lengkap", value = data.name, icon = Icons.Outlined.Person)
                ResultField(label = "Tempat Lahir", value = data.birthPlace ?: "-", icon = Icons.Outlined.Place)
                ResultField(label = "Tanggal Lahir", value = data.birthDate ?: "-", icon = Icons.Outlined.Cake)
                ResultField(label = "Jenis Kelamin", value = data.gender ?: "-", icon = Icons.Outlined.Wc)
                ResultField(label = "Agama", value = data.religion ?: "-", icon = Icons.Outlined.Church)
                ResultField(label = "Status Perkawinan", value = data.maritalStatus ?: "-", icon = Icons.Outlined.FamilyRestroom)
                ResultField(label = "Pekerjaan", value = data.occupation ?: "-", icon = Icons.Outlined.Work)
                ResultField(label = "Kewarganegaraan", value = data.nationality ?: "-", icon = Icons.Outlined.Flag)

                if (data.address != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardDark)
                    Text("Alamat", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    ResultField(label = "Alamat", value = data.address, icon = Icons.Outlined.Home)
                    ResultField(label = "RT/RW", value = data.rtRw ?: "-", icon = Icons.Outlined.LocationCity)
                    ResultField(label = "Kelurahan", value = data.village ?: "-", icon = Icons.Outlined.Apartment)
                    ResultField(label = "Kecamatan", value = data.district ?: "-", icon = Icons.Outlined.Map)
                    ResultField(label = "Kota/Kabupaten", value = data.city ?: "-", icon = Icons.Outlined.LocationOn)
                    ResultField(label = "Provinsi", value = data.province ?: "-", icon = Icons.Outlined.Public)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        ActionButtons(onScanAgain = onScanAgain, onViewHistory = onViewHistory)
    }
}

// ============================================================
// SHARED COMPONENTS
// ============================================================

@Composable
fun VerificationStatusBanner(
    isValid: Boolean,
    score: Float?,
    chipAuthPassed: Boolean? = null
) {
    val bgColor = if (isValid) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
    val borderColor = if (isValid) SuccessGreen else ErrorRed
    val icon = if (isValid) Icons.Filled.VerifiedUser else Icons.Filled.GppBad
    val statusText = if (isValid) "DOKUMEN TERVERIFIKASI" else "DOKUMEN TIDAK VALID"
    val statusColor = if (isValid) SuccessGreen else ErrorRed

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    statusText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (score != null) {
                    Text(
                        "Skor verifikasi: ${(score * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor.copy(alpha = 0.7f)
                    )
                }
                if (chipAuthPassed != null) {
                    Text(
                        if (chipAuthPassed) "Autentikasi chip berhasil" else "Autentikasi chip gagal",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ResultField(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp).offset(y = 2.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ChipStatusBadge(label: String, isValid: Boolean) {
    val color = if (isValid) SuccessGreen else ErrorRed
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isValid) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
fun ActionButtons(onScanAgain: () -> Unit, onViewHistory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Filled.Nfc, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Scan Kartu Lain", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SurfaceDark)
        ) {
            Icon(Icons.Outlined.History, contentDescription = null, tint = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text("Lihat Riwayat", color = TextSecondary)
        }
    }
}
