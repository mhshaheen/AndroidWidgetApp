package com.sm_fs.custommedia3player.data.interceptor

import android.util.Log
import com.sm_fs.custommedia3player.domain.callback.UrlResolver
import com.sm_fs.custommedia3player.domain.model.Track
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class UrlInterceptor(
    private val urlResolver: UrlResolver,
    private val getCurrentTrack: () -> Track?
) : Interceptor {
    
    companion object {
        private const val TAG = "UrlInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val track = getCurrentTrack() ?: return chain.proceed(request)

        return try {
            // Safe to use runBlocking here - OkHttp interceptors run on background threads
            val result = runBlocking { urlResolver.resolveUrl(track) }

            result.fold(
                onSuccess = { resolvedUrl ->
                    Log.d(TAG, "URL resolved: ${track.title}")
                    val newRequest = request.newBuilder()
                        .url(resolvedUrl)
                        .build()
                    chain.proceed(newRequest)
                },
                onFailure = { error ->
                    Log.e(TAG, "URL resolution failed for ${track.title}", error)
                    createErrorResponse(request, 500, error.message ?: "URL resolution failed")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Interceptor error", e)
            chain.proceed(request)
        }
    }
    
    private fun createErrorResponse(
        request: okhttp3.Request,
        code: Int,
        message: String
    ): Response {
        return Response.Builder()
            .code(code)
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .message(message)
            .body(message.toResponseBody(null))
            .build()
    }
}