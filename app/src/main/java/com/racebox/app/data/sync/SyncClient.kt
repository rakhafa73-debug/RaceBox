package com.racebox.app.data.sync

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SyncClient(
    baseUrl: String,
    private val usernameProvider: () -> String?
) {

    private val api: RaceBoxApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RaceBoxApi::class.java)

    private fun okHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val username = usernameProvider()
                val request = if (username.isNullOrEmpty()) {
                    original
                } else {
                    original.newBuilder()
                        .header("Authorization", "Bearer $username")
                        .build()
                }
                chain.proceed(request)
            }
            .build()

    suspend fun sync(payload: SyncPayload): Response<SyncResponse> = api.sync(payload)

    private companion object {
        const val CONNECT_TIMEOUT_SECONDS = 15L
        const val READ_TIMEOUT_SECONDS = 30L
    }
}