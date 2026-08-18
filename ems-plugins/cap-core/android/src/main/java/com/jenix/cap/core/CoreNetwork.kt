package com.jenix.cap.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object CoreNetwork {
    fun isAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
