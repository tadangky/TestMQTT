package com.tadev.android.testmqtt

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

fun getIP(): String {
    val ipAddress: String?
    try {
        val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val networkInterface: NetworkInterface = en.nextElement()
            val enumIpAddress: Enumeration<InetAddress> = networkInterface.inetAddresses
            while (enumIpAddress.hasMoreElements()) {
                val inetAddress: InetAddress = enumIpAddress.nextElement()
                if (inetAddress.isSiteLocalAddress) {
                    ipAddress = inetAddress.hostAddress.toString()
//                    Timber.e("ipAddress: $ipAddress")
                    return ipAddress
                }
            }
        }
    } catch (ex: SocketException) {
        return ""
    }
    return ""
}