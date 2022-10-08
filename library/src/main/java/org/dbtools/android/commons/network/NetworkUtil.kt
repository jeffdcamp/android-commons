@file:Suppress("unused")

package org.dbtools.android.commons.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

object NetworkUtil {
    @Suppress("ReturnCount")
    fun isConnected(context: Context, allowMobileNetwork: Boolean = true): Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT < 23) {
            isConnectedLegacy(connectivityManager, allowMobileNetwork)
        } else {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            val hasInternet = hasInternetCapabilityInAnyNetwork(context)

            // check to see if we even have Internet (no need to check mobile network capabilities if we don't even have Internet)
            if (!hasInternet) {
                return false
            }

            // check to see if mobile network is usable
            if (allowMobileNetwork) {
                // mobile network is allowed... so just check if we have Internet
                return hasInternet
            } else {
                // mobile network is NOT allowed...

                // check the default network for cellular connection
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return false
                }

                // check to see if vpn is using cellular connection
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && !hasInternetCapableWiFiInAnyNetwork(context)) {
                    // vpn network AND there was NO WiFi is in the list of transport networks
                    return false
                }

                // cellular network is NOT being used... so just check if we have Internet
                return hasInternet
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isConnectedLegacy(connectivityManager: ConnectivityManager, allowMeteredNetwork: Boolean = true): Boolean {
        val info = connectivityManager.activeNetworkInfo ?: return false

        val type = info.type
        return when {
            type == ConnectivityManager.TYPE_WIFI || type > ConnectivityManager.TYPE_WIMAX || allowMeteredNetwork -> info.isConnected || type == TYPE_WIFI_DIRECT
            else -> false
        }
    }

    fun isActiveNetworkMetered(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered
    }

    private fun hasInternetCapabilityInAnyNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
    }

    private fun hasInternetCapableWiFiInAnyNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    fun getAllNetworkInfo(context: Context, allowMobileNetwork: Boolean = true): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT < 23) {
            return "Legacy Network Checks (Android 5.1 or less)\n" +
                    "App can use Internet: ${isConnectedLegacy(connectivityManager, allowMobileNetwork)}\n" +
                    "\n" +
                    "=== Active Network ===\n" +
                    "${connectivityManager.activeNetworkInfo}"
        } else {
            val activeNetwork = connectivityManager.activeNetwork ?: return "No Active Network"
            val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?: return "No Network Capabilities"

            var info: String = "App can use Internet: ${isConnected(context, allowMobileNetwork)}\n" +
                    "Is Internet Available: ${activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}\n" +
                    "\n" +
                    "=== Active Network ===\n" +
                    "$activeNetworkCapabilities" +
                    "\n\n" +
                    "=== All Networks ===\n"

            connectivityManager.allNetworks.forEach { network ->
                connectivityManager.getNetworkCapabilities(network)?.let {
                    info += "$it\n\n"
                }
            }

            return info
        }
    }

    fun getActiveNetworkInfo(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT < 23) {
            return connectivityManager.activeNetworkInfo?.toString() ?: "No Active Network"
        } else {
            val activeNetwork = connectivityManager.activeNetwork ?: return "No Active Network"
            val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?: return "No Network Capabilities"
            return activeNetworkCapabilities.toString()
        }
    }

    /**
     * Registers a [ConnectivityManager.NetworkCallback] that can react to network connectivity changes. NOTE:
     * this is only supported on devices running API 21 or higher.
     */
    fun registerNetworkCallback(context: Context, networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }

    /**
     * Unregisters [ConnectivityManager.NetworkCallback] registered in [registerNetworkCallback]
     */
    fun unregisterNetworkCallback(context: Context, networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    const val DEFAULT_PORT = 80
    const val DEFAULT_SSL_PORT = 443
    const val DEFAULT_CONNECT_TIMEOUT = 30 * 1000 // ms
    const val DEFAULT_TIMEOUT = 30 * 1000 // ms
    const val DEFAULT_MAX_RETRY = 5
    const val TYPE_WIFI_DIRECT = 13 //WIFI_P2P
}
