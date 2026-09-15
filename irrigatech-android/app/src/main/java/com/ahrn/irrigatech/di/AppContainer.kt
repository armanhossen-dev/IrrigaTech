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
import com.ahrn.irrigatech.feedback.FeedbackApi
import com.ahrn.irrigatech.feedback.FeedbackRepository
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            },
        )
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            // Google Apps Script returns 302 redirect for POST requests.
            // OkHttp follows it as GET by default, but we need to preserve POST.
            if (response.code == 302 && request.url.host == "script.google.com") {
                val location = response.header("Location")
                if (location != null) {
                    response.close()
                    val newRequest = request.newBuilder()
                        .url(location)
                        .build()
                    return@addInterceptor chain.proceed(newRequest)
                }
            }
            response
        }
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

    private val feedbackApi: FeedbackApi = retrofit.create(FeedbackApi::class.java)

    val feedbackRepository: FeedbackRepository = FeedbackRepository(feedbackApi)

    val authRepository: AuthRepository = DefaultAuthRepository(
        context = context,
        session = sessionStore,
    )
}
