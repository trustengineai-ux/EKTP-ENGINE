package com.verihubs.nfcreader.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import com.verihubs.nfcreader.data.model.ChipType
import com.verihubs.nfcreader.data.model.NFCTagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility untuk membaca data dari NFC tag
 * Mendukung e-KTP (ISO 7816) dan berbagai format e-Certificate
 */
object NFCReader {

    /**
     * Baca data dari NFC Tag
     * Mengembalikan NFCTagData dengan raw hex data
     */
    suspend fun readTag(tag: Tag): Result<NFCTagData> = withContext(Dispatchers.IO) {
        try {
            val tagId = tag.id.toHexString()
            val techList = tag.techList.toList()

            // Tentukan chip type dan baca data sesuai tipe
            when {
                techList.contains("android.nfc.tech.IsoDep") -> readIsoDep(tag, tagId, techList)
                techList.contains("android.nfc.tech.NfcB") -> readNfcB(tag, tagId, techList)
                techList.contains("android.nfc.tech.NfcA") -> readNfcA(tag, tagId, techList)
                techList.contains("android.nfc.tech.Ndef") -> readNdef(tag, tagId, techList)
                techList.contains("android.nfc.tech.MifareClassic") -> readMifare(tag, tagId, techList)
                else -> Result.failure(Exception("Format NFC tidak didukung: $techList"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Baca ISO-DEP (digunakan oleh e-KTP Indonesia)
     * Menggunakan protokol ISO 7816-4 APDU
     */
    private fun readIsoDep(tag: Tag, tagId: String, techList: List<String>): Result<NFCTagData> {
        val isoDep = IsoDep.get(tag) ?: return Result.failure(Exception("Gagal mendapatkan IsoDep"))

        return try {
            isoDep.connect()
            isoDep.timeout = 10000 // 10 detik timeout

            val dataGroups = mutableMapOf<String, ByteArray>()
            val rawDataBuilder = StringBuilder()

            // SELECT Master File (MF)
            val selectMF = byteArrayOf(0x00, 0xA4.toByte(), 0x00, 0x00, 0x00)
            val mfResponse = isoDep.transceive(selectMF)
            rawDataBuilder.append("MF: ${mfResponse.toHexString()}\n")

            // SELECT Application - e-KTP Indonesia AID
            val ekTPAID = byteArrayOf(
                0xA0.toByte(), 0x00, 0x00, 0x00, 0x18,
                0x43, 0x49, 0x4E, 0x00, 0x00, 0x01, 0x63
            )
            val selectApp = buildSelectApdu(ekTPAID)
            val appResponse = isoDep.transceive(selectApp)
            rawDataBuilder.append("APP: ${appResponse.toHexString()}\n")

            // Baca Data Groups jika SELECT berhasil (SW = 90 00)
            if (isSuccessResponse(appResponse)) {
                // DG1 - Personal Data (EF.DG1)
                try {
                    val dg1Data = readDataGroup(isoDep, 0x01)
                    if (dg1Data != null) {
                        dataGroups["DG1"] = dg1Data
                        rawDataBuilder.append("DG1: ${dg1Data.toHexString()}\n")
                    }
                } catch (e: Exception) { /* DG1 tidak tersedia */ }

                // DG2 - Encoded Identification Features (EF.DG2) - foto
                try {
                    val dg2Data = readDataGroup(isoDep, 0x02)
                    if (dg2Data != null) {
                        dataGroups["DG2"] = dg2Data
                        rawDataBuilder.append("DG2: ${dg2Data.size} bytes (foto)\n")
                    }
                } catch (e: Exception) { /* DG2 tidak tersedia */ }

                // SOD - Security Object Document
                try {
                    val sodData = readDataGroup(isoDep, 0x1D)
                    if (sodData != null) {
                        dataGroups["SOD"] = sodData
                        rawDataBuilder.append("SOD: ${sodData.toHexString()}\n")
                    }
                } catch (e: Exception) { /* SOD tidak tersedia */ }
            }

            Result.success(
                NFCTagData(
                    tagId = tagId,
                    techList = techList,
                    rawDataHex = rawDataBuilder.toString(),
                    dataGroups = dataGroups,
                    chipType = ChipType.ISO_DEP
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca IsoDep: ${e.message}"))
        } finally {
            try { isoDep.close() } catch (e: Exception) { }
        }
    }

    /**
     * Baca NFC-B tags (beberapa e-Certificate menggunakan ini)
     */
    private fun readNfcB(tag: Tag, tagId: String, techList: List<String>): Result<NFCTagData> {
        val nfcB = NfcB.get(tag) ?: return Result.failure(Exception("Gagal mendapatkan NfcB"))
        return try {
            nfcB.connect()
            val appData = nfcB.applicationData
            val protInfo = nfcB.protocolInfo
            Result.success(
                NFCTagData(
                    tagId = tagId,
                    techList = techList,
                    rawDataHex = "AppData: ${appData?.toHexString()}\nProtInfo: ${protInfo?.toHexString()}",
                    chipType = ChipType.NFC_B
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca NfcB: ${e.message}"))
        } finally {
            try { nfcB.close() } catch (e: Exception) { }
        }
    }

    /**
     * Baca NFC-A tags
     */
    private fun readNfcA(tag: Tag, tagId: String, techList: List<String>): Result<NFCTagData> {
        val nfcA = NfcA.get(tag) ?: return Result.failure(Exception("Gagal mendapatkan NfcA"))
        return try {
            nfcA.connect()
            val atqa = nfcA.atqa
            val sak = nfcA.sak
            Result.success(
                NFCTagData(
                    tagId = tagId,
                    techList = techList,
                    rawDataHex = "ATQA: ${atqa?.toHexString()}\nSAK: $sak",
                    chipType = ChipType.NFC_A
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca NfcA: ${e.message}"))
        } finally {
            try { nfcA.close() } catch (e: Exception) { }
        }
    }

    /**
     * Baca NDEF tags (format umum untuk QR/NFC certificate)
     */
    private fun readNdef(tag: Tag, tagId: String, techList: List<String>): Result<NFCTagData> {
        val ndef = Ndef.get(tag) ?: return Result.failure(Exception("Gagal mendapatkan Ndef"))
        return try {
            ndef.connect()
            val message = ndef.ndefMessage
            val rawData = StringBuilder()
            message?.records?.forEachIndexed { index, record ->
                rawData.append("Record[$index]: ${record.payload.toHexString()}\n")
                rawData.append("Type: ${String(record.type)}\n")
            }
            Result.success(
                NFCTagData(
                    tagId = tagId,
                    techList = techList,
                    rawDataHex = rawData.toString(),
                    chipType = ChipType.NFC_A
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca Ndef: ${e.message}"))
        } finally {
            try { ndef.close() } catch (e: Exception) { }
        }
    }

    /**
     * Baca MIFARE Classic
     */
    private fun readMifare(tag: Tag, tagId: String, techList: List<String>): Result<NFCTagData> {
        val mifare = MifareClassic.get(tag) ?: return Result.failure(Exception("Gagal mendapatkan Mifare"))
        return try {
            mifare.connect()
            val rawData = StringBuilder()
            // Baca sector 0 (public data)
            if (mifare.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT)) {
                val blockCount = mifare.getBlockCountInSector(0)
                val firstBlock = mifare.sectorToBlock(0)
                for (i in 0 until blockCount) {
                    val blockData = mifare.readBlock(firstBlock + i)
                    rawData.append("Block${firstBlock + i}: ${blockData.toHexString()}\n")
                }
            }
            Result.success(
                NFCTagData(
                    tagId = tagId,
                    techList = techList,
                    rawDataHex = rawData.toString(),
                    chipType = ChipType.MIFARE_CLASSIC
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca Mifare: ${e.message}"))
        } finally {
            try { mifare.close() } catch (e: Exception) { }
        }
    }

    // ===== HELPER FUNCTIONS =====

    private fun readDataGroup(isoDep: IsoDep, dgNumber: Int): ByteArray? {
        val selectDG = buildSelectEF(dgNumber)
        val selectResponse = isoDep.transceive(selectDG)
        if (!isSuccessResponse(selectResponse)) return null

        // Baca dengan READ BINARY
        val readCmd = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0xFF.toByte())
        val data = isoDep.transceive(readCmd)
        if (data.size < 2) return null

        val sw = data.takeLast(2).toByteArray().toHexString()
        return if (sw == "9000") data.dropLast(2).toByteArray() else null
    }

    private fun buildSelectApdu(aid: ByteArray): ByteArray {
        return byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid + byteArrayOf(0x00)
    }

    private fun buildSelectEF(efId: Int): ByteArray {
        val highByte = ((efId shr 8) and 0xFF).toByte()
        val lowByte = (efId and 0xFF).toByte()
        return byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, highByte, lowByte)
    }

    private fun isSuccessResponse(response: ByteArray): Boolean {
        if (response.size < 2) return false
        val sw1 = response[response.size - 2].toInt() and 0xFF
        val sw2 = response[response.size - 1].toInt() and 0xFF
        return sw1 == 0x90 && sw2 == 0x00
    }

    fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }
    fun List<Byte>.toByteArray(): ByteArray = ByteArray(size) { this[it] }
}
