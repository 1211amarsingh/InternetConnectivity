package com.example.internetlib

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class NetworkInfo(private val context: Context) : BroadcastReceiver() {

    private lateinit var network: Network
    private val TAG = "NetworkInfo";

    // collection of listeners
    private val listeners = mutableSetOf<NetworkInfoListener>()

    // constructor
    init {
        context.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        network = Network()
    }

    // receive network changes
    override fun onReceive(context: Context, intent: Intent) = runBlocking {
        Log.d(TAG, "onReceive")
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val job = launch {
            // verify network availability
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                Log.d(TAG, "Network available")
                // verify internet access
                if (hostAvailable("google.com", 80)) {
                    // internet access
                    Log.d(TAG, "Internet Access Detected")
                    network.status = NetworkStatus.INTERNET
                } else {
                    // no internet access
                    Log.d(TAG, "Unable to access Internt")
                    network.status = NetworkStatus.OFFLINE
                }
                // get network type
                when (activeNetwork.type) {
                    ConnectivityManager.TYPE_MOBILE -> {
                        // mobile network
                        Log.d(TAG, "Connectivity: MOBILE")
                        network.type = NetworkType.MOBILE
                    }
                    ConnectivityManager.TYPE_WIFI -> {
                        // wifi network
                        Log.d(TAG, "Connectivity: WIFI")
                        network.type = NetworkType.WIFI
                    }
                    else -> {
                        // no network available
                        Log.d(TAG, "Network not available")
                        network.type = NetworkType.NONE
                    }
                }
            } else {
                // no network available
                Log.d(TAG, "Network not available")
                network.type = NetworkType.NONE
                network.status = NetworkStatus.OFFLINE
            }
        }
        job.join()
        notifyNetworkChangeToAll()
    }

    // verify host availability
    private fun hostAvailable(host: String, port: Int): Boolean {
        Log.d(TAG, "Verifying host availability: $host:$port")
        try {
            Socket().use({ socket ->
                socket.connect(InetSocketAddress(host, port), 2000)
                socket.close()
                // host available
                Log.d(TAG, "Host: $host:$port is available")
                return true
            })
        } catch (e: IOException) {
            // host unreachable or timeout
            Log.d(TAG, "Host: $host:$port is not available")
            return false
        }
    }

    // notify network change to all listeners
    private fun notifyNetworkChangeToAll() {
        Log.d(TAG, "notifyStateToAll")
        for (listener in listeners) {
            notifyNetworkChange(listener)
        }
    }

    // notify network change
    private fun notifyNetworkChange(listener: NetworkInfoListener) {
        Log.d(TAG, "notifyState")
        listener.networkStatusChange(network)
    }

    // add a listener
    fun addListener(listener: NetworkInfoListener) {
        Log.d(TAG, "addListener")
        listeners.add(listener)
        notifyNetworkChange(listener)
    }

    // remove a listener
    fun removeListener(listener: NetworkInfoListener) {
        Log.d(TAG, "removeListener")
        listeners.remove(listener)
    }

    // get current network information
    fun getNetwork(): Network {
        return network
    }

    // static content
    companion object {
        @SuppressLint("StaticFieldLeak")

        private var networkInfo: NetworkInfo? = null

        // get a singleton
        @JvmStatic
        public fun getInstance(ctx: Context): NetworkInfo {
            if (networkInfo == null) {
                networkInfo = NetworkInfo(ctx.applicationContext)
            }
            return networkInfo as NetworkInfo
        }
    }

    // interface that represent the [NetworkStatusListener]
    interface NetworkInfoListener {
        fun networkStatusChange(network: Network)
    }

    data class Network(
            var type: NetworkType = NetworkType.NONE,
            var status: NetworkStatus = NetworkStatus.OFFLINE
    )

    enum class NetworkType { NONE, WIFI, MOBILE }

    enum class NetworkStatus { OFFLINE, INTERNET }
}