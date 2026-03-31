package com.verihubs.nfcreader.data.model

import com.google.gson.annotations.SerializedName

// ===== REQUEST MODELS =====

data class ECertificateVerifyRequest(
    @SerializedName("chip_data") val chipData: String,          // Data hex dari NFC chip
    @SerializedName("certificate_id") val certificateId: String?, // ID sertifikat (opsional)
    @SerializedName("document_type") val documentType: String = "ECERTIFICATE",
    @SerializedName("additional_info") val additionalInfo: Map<String, String>? = null
)

data class EKTPVerifyRequest(
    @SerializedName("chip_data") val chipData: String,           // Raw chip data dari e-KTP
    @SerializedName("nik") val nik: String? = null,              // NIK untuk cross-check
    @SerializedName("dg1") val dg1: String? = null,              // Data Group 1 (personal info)
    @SerializedName("dg2") val dg2: String? = null,              // Data Group 2 (foto)
    @SerializedName("sod") val sod: String? = null,              // Security Object Document
    @SerializedName("com") val com: String? = null               // Common Object
)

data class DocumentLivenessRequest(
    @SerializedName("chip_data") val chipData: String,
    @SerializedName("document_type") val documentType: String
)

// ===== RESPONSE MODELS =====

data class ECertificateVerifyResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ECertificateData?,
    @SerializedName("request_id") val requestId: String?
)

data class ECertificateData(
    @SerializedName("certificate_id") val certificateId: String,
    @SerializedName("holder_name") val holderName: String,
    @SerializedName("holder_nik") val holderNik: String?,
    @SerializedName("certificate_type") val certificateType: String,
    @SerializedName("issued_by") val issuedBy: String,
    @SerializedName("issued_date") val issuedDate: String,
    @SerializedName("expired_date") val expiredDate: String?,
    @SerializedName("is_valid") val isValid: Boolean,
    @SerializedName("verification_score") val verificationScore: Float?,
    @SerializedName("certificate_number") val certificateNumber: String?,
    @SerializedName("additional_data") val additionalData: Map<String, String>?
)

data class EKTPVerifyResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: EKTPData?,
    @SerializedName("request_id") val requestId: String?
)

data class EKTPData(
    @SerializedName("nik") val nik: String,
    @SerializedName("name") val name: String,
    @SerializedName("birth_place") val birthPlace: String?,
    @SerializedName("birth_date") val birthDate: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("rt_rw") val rtRw: String?,
    @SerializedName("village") val village: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("province") val province: String?,
    @SerializedName("religion") val religion: String?,
    @SerializedName("marital_status") val maritalStatus: String?,
    @SerializedName("occupation") val occupation: String?,
    @SerializedName("nationality") val nationality: String?,
    @SerializedName("photo_base64") val photoBase64: String?,
    @SerializedName("is_chip_valid") val isChipValid: Boolean,
    @SerializedName("chip_auth_passed") val chipAuthPassed: Boolean
)

data class CertificateDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ECertificateData?
)

data class DocumentLivenessResponse(
    @SerializedName("status") val status: String,
    @SerializedName("is_live") val isLive: Boolean,
    @SerializedName("confidence") val confidence: Float?,
    @SerializedName("message") val message: String
)

// ===== LOCAL DATABASE MODEL =====

data class ScanHistoryItem(
    val id: Long = 0,
    val scanType: ScanType,
    val holderName: String,
    val documentId: String,
    val isVerified: Boolean,
    val verificationScore: Float?,
    val scannedAt: Long = System.currentTimeMillis(),
    val rawData: String? = null
)

enum class ScanType {
    E_CERTIFICATE,
    E_KTP,
    UNKNOWN
}

// ===== NFC STATE =====

data class NFCTagData(
    val tagId: String,
    val techList: List<String>,
    val rawDataHex: String,
    val dataGroups: Map<String, ByteArray> = emptyMap(),
    val chipType: ChipType = ChipType.UNKNOWN
)

enum class ChipType {
    ISO_DEP,
    NFC_A,
    NFC_B,
    MIFARE_CLASSIC,
    UNKNOWN
}
