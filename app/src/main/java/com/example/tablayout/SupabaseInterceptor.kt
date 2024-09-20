package com.example.tablayout
import okhttp3.Interceptor
import okhttp3.Response

class SupabaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9kYmRkd2R3a2xoZWJudmd2d2x2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjY1OTEwNDQsImV4cCI6MjA0MjE2NzA0NH0.mgOCZJHa3Mnb2ISsUEeoa4rUeTTtDvfceeZQPPSMnHI")
            .build()
        return chain.proceed(request)
    }
}
