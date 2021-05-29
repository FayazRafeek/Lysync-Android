package com.fayazmohamed.lysync.Api

import android.content.Context
import com.fayazmohamed.lysync.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    private lateinit var apiService: ApiEndpoints
    lateinit var sessionManager: SessionManager

    fun getApiService(context: Context): ApiEndpoints {
        
        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {

            val retrofit = Retrofit.Builder()
                .baseUrl("http://65.2.11.148:3000/api/")
                .client(okhttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            apiService = retrofit.create(ApiEndpoints::class.java)
        }
        
        return apiService
    }

    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
    }

    class AuthInterceptor(context: Context) : Interceptor {
        private val sessionManager = SessionManager(context)

        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()

            // If token has been saved, add it to the request
            sessionManager.fetchAuthToken()?.let {
                requestBuilder.addHeader("x-access-token", it)
            }

            return chain.proceed(requestBuilder.build())
        }
    }
    
}