//package com.example.androidwidgetapp.shortsPlayer
//
//import com.shadhin.shorts.util.FAKE_PLAY_URL_HOST
//import okhttp3.Interceptor
//import okhttp3.Response
//
//class PlayerInterceptor : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val original = chain.request()
//        val url = original.url.toString()
//
//        // Only intercept URLs with your custom host
//        return if (url.contains(FAKE_PLAY_URL_HOST)) {
//            // Replace the URL with the real stream URL
//            val newUrl = url.replace(FAKE_PLAY_URL_HOST, "samplelib.com/lib/preview")
//            val newRequest = original.newBuilder()
//                .url(newUrl)
//                .header("User-Agent", "SM Shorts Player")
//                .build()
//            chain.proceed(newRequest)
//        } else {
//            // For all other URLs, proceed normally
//            chain.proceed(original)
//        }
//    }
//}