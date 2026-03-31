package com.verihubs.nfcreader.data.api

import com.verihubs.nfcreader.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface VerihubsApiService {

    /**
     * Verifikasi e-Certificate menggunakan data NFC yang dibaca dari kartu
     * Endpoint: POST /ecertificate/verify
     */
    @POST("ecertificate/verify")
    suspend fun verifyECertificate(
        @Body request: ECertificateVerifyRequest
    ): Response<ECertificateVerifyResponse>

    /**
     * Verifikasi KTP Elektronik (e-KTP) via NFC
     * Endpoint: POST /ekyc/nfc
     */
    @POST("ekyc/nfc")
    suspend fun verifyEKTP(
        @Body request: EKTPVerifyRequest
    ): Response<EKTPVerifyResponse>

    /**
     * Ambil detail certificate berdasarkan ID
     * Endpoint: GET /ecertificate/{certificateId}
     */
    @GET("ecertificate/{certificateId}")
    suspend fun getCertificateDetail(
        @Path("certificateId") certificateId: String
    ): Response<CertificateDetailResponse>

    /**
     * Liveness check - verifikasi keaslian dokumen
     * Endpoint: POST /document/liveness
     */
    @POST("document/liveness")
    suspend fun documentLiveness(
        @Body request: DocumentLivenessRequest
    ): Response<DocumentLivenessResponse>
}
