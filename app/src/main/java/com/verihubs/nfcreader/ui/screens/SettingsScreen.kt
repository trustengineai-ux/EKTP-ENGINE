package com.verihubs.nfcreader.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.verihubs.nfcreader.BuildConfig
import com.verihubs.nfcreader.nfc.NFCViewModel
import com.verihubs.nfcreader.ui.theme.*

@Composable
fun SettingsScreen(viewModel: NFCViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "Pengaturan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // API Info
        SettingsSection(title = "API Verihubs") {
            SettingsInfoItem(
                icon = Icons.Outlined.Key,
                label = "API Key",
                value = "••••••••${BuildConfig.VERIHUBS_API_KEY.takeLast(4)}"
            )
            SettingsInfoItem(
                icon = Icons.Outlined.Cloud,
                label = "Base URL",
                value = BuildConfig.VERIHUBS_BASE_URL
            )
            SettingsInfoItem(
                icon = Icons.Outlined.Info,
                label = "Versi API",
                value = "v1"
            )
        }

        Spacer(Modifier.height(16.dp))

        // App info
        SettingsSection(title = "Aplikasi") {
            SettingsInfoItem(
                icon = Icons.Outlined.PhoneAndroid,
                label = "Versi Aplikasi",
                value = "1.0.0"
            )
            SettingsInfoItem(
                icon = Icons.Outlined.Code,
                label = "Bahasa",
                value = "Kotlin + Jetpack Compose"
            )
            SettingsInfoItem(
                icon = Icons.Outlined.Nfc,
                label = "NFC Protocol",
                value = "ISO 7816-4 / ISO 14443"
            )
        }

        Spacer(Modifier.height(16.dp))

        // About
        SettingsSection(title = "Tentang") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "NFC Verihubs Reader",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aplikasi pembaca NFC untuk verifikasi e-KTP dan e-Sertifikat menggunakan API Verihubs Indonesia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "verihubs.com",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Surface(shape = RoundedCornerShape(16.dp), color = SurfaceDark, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}
