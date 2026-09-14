package com.ahrn.irrigatech.di

import android.content.Context
import com.ahrn.irrigatech.data.auth.AuthRepository
import com.ahrn.irrigatech.data.auth.DefaultAuthRepository
import com.ahrn.irrigatech.data.local.DeviceConfigStore
import com.ahrn.irrigatech.data.local.SecureTokenStore
import com.ahrn.irrigatech.data.local.SessionStore
import com.ahrn.irrigatech.data.local.SettingsStore
import com.ahrn.irrigatech.data.remote.BlynkApi
import com.ahrn.irrigatech.data.remote.BlynkDataSource
import com.ahrn.irrigatech.data.remote.MockDataSource
import com.ahrn.irrigatech.data.repository.AlertRepository
import com.ahrn.irrigatech.data.repository.DefaultSensorRepository
import com.ahrn.irrigatech.data.repository.SensorRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Hand-rolled dependency graph. The app is small enough that a DI framework
 * would only add size and build time, so singletons are wired by hand here.
 */
class AppContainer(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            },
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://blynk.cloud/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val settingsStore: SettingsStore = SettingsStore(context)
    val deviceStore: DeviceConfigStore = DeviceConfigStore(context)
    val tokenStore: SecureTokenStore = SecureTokenStore(context)
    val sessionStore: SessionStore = SessionStore(context)
    val alertRepository: AlertRepository = AlertRepository(context)

    private val blynkApi: BlynkApi = retrofit.create(BlynkApi::class.java)
    private val blynkDataSource = BlynkDataSource(blynkApi)
    private val mockDataSource = MockDataSource()

    val sensorRepository: SensorRepository = DefaultSensorRepository(
        blynk = blynkDataSource,
        mock = mockDataSource,
    )

    val authRepository: AuthRepository = DefaultAuthRepository(
        context = context,
        session = sessionStore,
    )
}
