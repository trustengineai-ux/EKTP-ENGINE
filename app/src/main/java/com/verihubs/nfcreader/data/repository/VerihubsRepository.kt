package com.verihubs.nfcreader.data.repository

import com.verihubs.nfcreader.data.api.VerihubsApiClient
import com.verihubs.nfcreader.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

@Singleton
class VerihubsRepository @Inject constructor(
    private val apiClient: VerihubsApiClient
) {

    /**
     * Verifikasi e-Certificate dari data NFC
     */
    fun verifyECertificate(chipDataHex: String, certificateId: String? = null): Flow<ApiResult<ECertificateData>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiClient.service.verifyECertificate(
                ECertificateVerifyRequest(
                    chipData = chipDataHex,
                    certificateId = certificateId
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data != null) {
                    emit(ApiResult.Success(body.data))
                } else {
                    emit(ApiResult.Error(response.code(), body?.message ?: "Data tidak ditemukan"))
                }
            } else {
                emit(ApiResult.Error(response.code(), parseErrorMessage(response.code())))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(-1, e.message ?: "Terjadi kesalahan koneksi"))
        }
    }

    /**
     * Verifikasi e-KTP dari data NFC chip
     */
    fun verifyEKTP(chipDataHex: String, nik: String? = null): Flow<ApiResult<EKTPData>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiClient.service.verifyEKTP(
                EKTPVerifyRequest(chipData = chipDataHex, nik = nik)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data != null) {
                    emit(ApiResult.Success(body.data))
                } else {
                    emit(ApiResult.Error(response.code(), body?.message ?: "Data KTP tidak ditemukan"))
                }
            } else {
                emit(ApiResult.Error(response.code(), parseErrorMessage(response.code())))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(-1, e.message ?: "Terjadi kesalahan koneksi"))
        }
    }

    /**
     * Cek liveness dokumen
     */
    fun checkDocumentLiveness(chipDataHex: String, documentType: String): Flow<ApiResult<DocumentLivenessResponse>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiClient.service.documentLiveness(
                DocumentLivenessRequest(chipData = chipDataHex, documentType = documentType)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(ApiResult.Success(body))
                } else {
                    emit(ApiResult.Error(response.code(), "Response kosong"))
                }
            } else {
                emit(ApiResult.Error(response.code(), parseErrorMessage(response.code())))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(-1, e.message ?: "Terjadi kesalahan koneksi"))
        }
    }

    private fun parseErrorMessage(code: Int): String = when (code) {
        400 -> "Request tidak valid"
        401 -> "API Key tidak valid atau belum diatur"
        403 -> "Akses ditolak"
        404 -> "Data tidak ditemukan"
        422 -> "Data tidak dapat diproses"
        429 -> "Terlalu banyak request, coba lagi"
        500 -> "Server error, coba lagi nanti"
        else -> "Error tidak diketahui (kode: $code)"
    }
}
