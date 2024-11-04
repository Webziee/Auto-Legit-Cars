package com.legitautocars.app

import okhttp3.Interceptor
import okhttp3.Response

/**
 * SupabaseInterceptor class to handle API key authentication for requests to Supabase.
 *
 * The following code structure has been adapted based on general
 * tutorials and examples from official documentation, as well as resources from:
 *
 * - Supabase Documentation: https://supabase.com/docs
 * - OkHttp Documentation: https://square.github.io/okhttp/
 *
 * This interceptor is responsible for adding the API key to each request header,
 * allowing authenticated communication with Supabase.
 */

class SupabaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9kYmRkd2R3a2xoZWJudmd2d2x2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjY1OTEwNDQsImV4cCI6MjA0MjE2NzA0NH0.mgOCZJHa3Mnb2ISsUEeoa4rUeTTtDvfceeZQPPSMnHI")
            .build()
        return chain.proceed(request)
    }
}
