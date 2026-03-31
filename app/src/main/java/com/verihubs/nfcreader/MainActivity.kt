package com.verihubs.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.verihubs.nfcreader.ui.screens.MainScreen
import com.verihubs.nfcreader.ui.theme.NFCVerihubsTheme
import com.verihubs.nfcreader.nfc.NFCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val nfcViewModel: NFCViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Setup Foreground Dispatch untuk menangkap NFC saat app aktif
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        setContent {
            NFCVerihubsTheme {
                MainScreen(
                    nfcAdapter = nfcAdapter,
                    viewModel = nfcViewModel
                )
            }
        }

        // Handle jika app dibuka dari NFC intent
        handleNFCIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            if (!adapter.isEnabled) return

            val filters = arrayOf(
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            )

            val techLists = arrayOf(
                arrayOf("android.nfc.tech.IsoDep"),
                arrayOf("android.nfc.tech.NfcA"),
                arrayOf("android.nfc.tech.NfcB"),
                arrayOf("android.nfc.tech.Ndef"),
                arrayOf("android.nfc.tech.MifareClassic"),
                arrayOf("android.nfc.tech.MifareUltralight")
            )

            adapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNFCIntent(intent)
    }

    private fun handleNFCIntent(intent: Intent) {
        val action = intent.action
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let { nfcViewModel.processNFCTag(it) }
        }
    }
}
