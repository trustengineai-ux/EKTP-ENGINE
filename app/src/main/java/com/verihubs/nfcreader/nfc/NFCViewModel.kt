package com.verihubs.nfcreader.nfc

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verihubs.nfcreader.data.model.*
import com.verihubs.nfcreader.data.repository.ApiResult
import com.verihubs.nfcreader.data.repository.VerihubsRepository
import com.verihubs.nfcreader.nfc.NFCReader.toHexString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// State untuk UI
sealed class NFCScanState {
    data object Idle : NFCScanState()
    data object WaitingForCard : NFCScanState()
    data object ReadingCard : NFCScanState()
    data object Verifying : NFCScanState()
    data class CardRead(val tagData: NFCTagData) : NFCScanState()
    data class ECertificateVerified(val data: ECertificateData) : NFCScanState()
    data class EKTPVerified(val data: EKTPData) : NFCScanState()
    data class Error(val message: String) : NFCScanState()
}

data class NFCUiState(
    val scanState: NFCScanState = NFCScanState.Idle,
    val scanHistory: List<ScanHistoryItem> = emptyList(),
    val selectedScanMode: ScanMode = ScanMode.AUTO_DETECT,
    val isNFCEnabled: Boolean = true
)

enum class ScanMode {
    AUTO_DETECT,
    E_CERTIFICATE,
    E_KTP
}

@HiltViewModel
class NFCViewModel @Inject constructor(
    private val repository: VerihubsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NFCUiState())
    val uiState: StateFlow<NFCUiState> = _uiState.asStateFlow()

    private val _scanHistory = mutableListOf<ScanHistoryItem>()

    fun setScanMode(mode: ScanMode) {
        _uiState.update { it.copy(selectedScanMode = mode) }
    }

    fun setNFCEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isNFCEnabled = enabled) }
    }

    fun startWaiting() {
        _uiState.update { it.copy(scanState = NFCScanState.WaitingForCard) }
    }

    fun resetState() {
        _uiState.update { it.copy(scanState = NFCScanState.Idle) }
    }

    /**
     * Proses NFC tag yang terdeteksi
     */
    fun processNFCTag(tag: Tag) {
        viewModelScope.launch {
            _uiState.update { it.copy(scanState = NFCScanState.ReadingCard) }

            // Baca data dari NFC chip
            val result = NFCReader.readTag(tag)

            result.fold(
                onSuccess = { tagData ->
                    _uiState.update { it.copy(scanState = NFCScanState.CardRead(tagData)) }

                    // Verifikasi berdasarkan mode yang dipilih
                    when (_uiState.value.selectedScanMode) {
                        ScanMode.E_CERTIFICATE -> verifyAsECertificate(tagData)
                        ScanMode.E_KTP -> verifyAsEKTP(tagData)
                        ScanMode.AUTO_DETECT -> autoDetectAndVerify(tagData)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(scanState = NFCScanState.Error(
                            error.message ?: "Gagal membaca kartu"
                        ))
                    }
                }
            )
        }
    }

    /**
     * Auto-detect tipe dokumen dan verifikasi
     */
    private suspend fun autoDetectAndVerify(tagData: NFCTagData) {
        // Deteksi berdasarkan struktur data
        val isEKTP = tagData.dataGroups.containsKey("DG1") || tagData.dataGroups.containsKey("DG2")
        if (isEKTP) {
            verifyAsEKTP(tagData)
        } else {
            verifyAsECertificate(tagData)
        }
    }

    /**
     * Verifikasi sebagai e-Certificate via Verihubs API
     */
    private suspend fun verifyAsECertificate(tagData: NFCTagData) {
        _uiState.update { it.copy(scanState = NFCScanState.Verifying) }

        repository.verifyECertificate(tagData.rawDataHex).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(scanState = NFCScanState.Verifying) }
                }
                is ApiResult.Success -> {
                    val certData = result.data
                    _uiState.update { state ->
                        state.copy(scanState = NFCScanState.ECertificateVerified(certData))
                    }
                    addToHistory(
                        ScanHistoryItem(
                            scanType = ScanType.E_CERTIFICATE,
                            holderName = result.data.holderName,
                            documentId = result.data.certificateId,
                            isVerified = result.data.isValid,
                            verificationScore = result.data.verificationScore,
                            rawData = tagData.rawDataHex
                        )
                    )
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(scanState = NFCScanState.Error(result.message))
                    }
                }
            }
        }
    }

    /**
     * Verifikasi sebagai e-KTP via Verihubs API
     */
    private suspend fun verifyAsEKTP(tagData: NFCTagData) {
        _uiState.update { it.copy(scanState = NFCScanState.Verifying) }

        repository.verifyEKTP(tagData.rawDataHex).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(scanState = NFCScanState.Verifying) }
                }
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(scanState = NFCScanState.EKTPVerified(result.data))
                    }
                    addToHistory(
                        ScanHistoryItem(
                            scanType = ScanType.E_KTP,
                            holderName = result.data.name,
                            documentId = result.data.nik,
                            isVerified = result.data.isChipValid,
                            verificationScore = null,
                            rawData = tagData.rawDataHex
                        )
                    )
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(scanState = NFCScanState.Error(result.message))
                    }
                }
            }
        }
    }

    private fun addToHistory(item: ScanHistoryItem) {
        _scanHistory.add(0, item)
        _uiState.update { it.copy(scanHistory = _scanHistory.toList()) }
    }
}
