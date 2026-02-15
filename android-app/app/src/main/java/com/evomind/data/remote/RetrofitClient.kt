package com.evomind.data.remote

import com.evomind.data.remote.dto.ApiResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit网络客户端
 * 提供API服务创建功能
 */
@Singleton
class RetrofitClient @Inject constructor() {

    companion object {
        // 开发环境后端地址
        private const val BASE_URL = "http://10.0.2.2:8080/"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * 创建API服务
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}

/**
 * 带有认证Token的API服务创建
 */
@Singleton
class AuthRetrofitClient @Inject constructor(
    private val tokenManager: TokenManager
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val token = tokenManager.getToken()
            val request = if (token != null) {
                original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                    .build()
            } else {
                original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
            }
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}

/**
 * Token管理器
 * 用于存储和获取认证Token
 */
@Singleton
class TokenManager @Inject constructor() {
    // 实际项目中应该使用DataStore或SharedPreferences存储
    private var token: String? = null

    fun saveToken(token: String) {
        this.token = token
    }

    fun getToken(): String? = token

    fun clearToken() {
        token = null
    }

    fun isLoggedIn(): Boolean = token != null
}
