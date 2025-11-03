package com.sm_fs.custommedia3player.core.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.media3.common.util.UnstableApi

@UnstableApi
class MediaServiceConnectionHandler(
    private val onConnected: (GenericMediaService) -> Unit,
    private val onDisconnected: () -> Unit
) : ServiceConnection {
    
    companion object {
        private const val TAG = "ServiceConnection"
    }
    
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d(TAG, "Service connected")
        if (service is GenericMediaService.ServiceBinder) {
            onConnected(service.getService())
        }
    }
    
    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d(TAG, "Service disconnected")
        onDisconnected()
    }
}