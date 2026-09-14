package com.ahrn.irrigatech.data.remote

import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Blynk IoT Cloud REST endpoints.
 *
 * Read:  https://blynk.cloud/external/api/get?token={TOKEN}&V0
 * Write: https://blynk.cloud/external/api/update?token={TOKEN}&V2=1
 *
 * The full URL is built by [BlynkDataSource]; @Url keeps the pin query
 * construction explicit and safe.
 */
interface BlynkApi {

    @GET
    suspend fun get(@Url url: String): JsonElement

    @GET
    suspend fun update(@Url url: String): JsonElement

    @GET
    suspend fun isHardwareConnected(@Url url: String): JsonElement
}
