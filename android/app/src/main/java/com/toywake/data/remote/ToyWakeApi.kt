package com.toywake.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ToyWakeApi {
    @GET("api/v1/health")
    suspend fun health(): HealthDto

    @POST("api/v1/scan")
    suspend fun scan(@Body req: ScanRequest): ScanResponseDto

    @POST("api/v1/toys")
    suspend fun createToy(@Body req: ToyCreateRequest): ToyCreateResponseDto

    @POST("api/v1/play/start")
    suspend fun playStart(@Body req: PlayStartRequest): PlayStartResponseDto

    @POST("api/v1/play/next")
    suspend fun playNext(@Body req: PlayNextRequest): PlayNextResponseDto

    @POST("api/v1/play/end")
    suspend fun playEnd(@Body req: PlayEndRequest): PlayEndResponseDto

    @POST("api/v1/memories")
    suspend fun createMemory(@Body req: MemoryCreateRequest): MemoryCreateResponseDto
}
