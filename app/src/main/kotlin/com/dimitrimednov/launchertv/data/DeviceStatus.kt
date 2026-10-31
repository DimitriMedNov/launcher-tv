package com.dimitrimednov.launchertv.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Conexión a internet y estado del wifi (solo lectura: desde Android 10 una app no puede apagar el wifi). */
class DeviceStatus(private val context: Context) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val wifi = context.applicationContext.getSystemService(WifiManager::class.java)

    fun online(): Flow<Boolean> = callbackFlow {
        fun current() = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
            override fun onLost(network: Network) { trySend(current()) }
        }
        trySend(current())
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    fun wifiEnabled(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) { trySend(wifi.isWifiEnabled) }
        }
        trySend(wifi.isWifiEnabled)
        context.registerReceiver(receiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
