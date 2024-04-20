package com.antoineabf.project451;

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.Manifest
import android.net.NetworkCapabilities
import java.lang.Math.pow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface
public class CaptureInfo {
    fun getOperatorName(context: Context): String? {
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.networkOperatorName
    }
    fun getSignalStrength(context: Context) :String {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cellInfoList = telephonyManager.allCellInfo
                if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                    var maxSignalStrength = Int.MIN_VALUE
                    for (cellInfo in cellInfoList) {
                        if (cellInfo is CellInfoGsm) {
                            val signalStrength = cellInfo.cellSignalStrength.dbm
                            if (signalStrength > maxSignalStrength) {
                                maxSignalStrength = signalStrength
                            }
                        } else if (cellInfo is CellInfoLte) {
                            val signalStrength = cellInfo.cellSignalStrength.dbm
                            if (signalStrength > maxSignalStrength) {
                                maxSignalStrength = signalStrength
                            }
                        } else if (cellInfo is CellInfoWcdma) {
                            val signalStrength = cellInfo.cellSignalStrength.dbm
                            if (signalStrength > maxSignalStrength) {
                                maxSignalStrength = signalStrength
                            }
                        }
                    }
                    return maxSignalStrength.toString() + " dBm"
                }
            } else {
                return "Not Available"
            }
        return "Not Available"


    }
    fun getSNR(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val cellInfoList = telephonyManager.allCellInfo

                for (cellInfo in cellInfoList) {
                    when (cellInfo) {
                        is CellInfoGsm -> {
                            //For 2G
                            return "Not Available"
                        }
                        is CellInfoWcdma -> {
                            // For 3G (UMTS/WCDMA)
                            return "Not Available"
                        }
                        is CellInfoLte -> {
                            // For 4G (LTE)
                            val lteCell = cellInfo.cellSignalStrength
                            val rssnr = lteCell.rssnr // Signal to Noise Ratio in dB
                            return rssnr.toString() + " dB"
                        }

                    }
                }
            } else {
                return "Not Available"
            }
        }
        return "Not Available"
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun getNetworkType(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cellInfoList = telephonyManager.allCellInfo

            if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                for (cellInfo in cellInfoList) {
                    when (cellInfo) {
                        is CellInfoLte -> {
                            return "4G"
                        }
                        is CellInfoWcdma -> {
                            return "3G"
                        }
                        is CellInfoGsm -> {
                            return "2G"
                        }
                    }
                }
            }
        } else {
            return "Not Available"
        }
        return "Not Available"
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun getFrequencyBand(context: Context): String {
        // Frequency Band (if available)
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cellInfoList = telephonyManager.allCellInfo

            if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                for (cellInfo in cellInfoList) {
                    when (cellInfo) {
                        is CellInfoLte -> {
                            val cellIdentity = cellInfo.cellIdentity
                            // val frequency = cellIdentity.earfcn * 0.1f + 0.9f
                            return "${cellIdentity.earfcn} (${round(cellIdentity.bandwidth*pow(10.0,-6.0)).toInt()}MHz)"
                        }
                        is CellInfoWcdma -> {
                            val cellIdentity = cellInfo.cellIdentity
                            return "${cellIdentity.uarfcn} (${round(cellIdentity.uarfcn * 0.2f).toInt()}MHz)"
                        }
                        is CellInfoGsm -> {
                            val cellIdentity = cellInfo.cellIdentity
                            return "${cellIdentity.arfcn} (${round(cellIdentity.arfcn * 0.2f + 935).toInt()}MHz)"
                        }
                    }
                }
            }
        } else {
            return "Not Available"
        }
        return "Not Available"
    }
    fun getCellID(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Check for permission before accessing cell information
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cellInfoList = telephonyManager.allCellInfo

            if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                for (cellInfo in cellInfoList) {
                    return when (cellInfo) {
                        is CellInfoGsm -> cellInfo.cellIdentity.cid.toString()
                        is CellInfoLte -> cellInfo.cellIdentity.ci.toString()
                        is CellInfoWcdma -> cellInfo.cellIdentity.cid.toString()
                        else -> "Not Available" // Handle other cell types or unknown cases
                    }
                }
            }
        } else {

            return "Not Available" // Default value if cell ID cannot be retrieved
        }
        return "Not Available"// Default value if cell ID cannot be retrieved
    }
    fun getTimestamp(): String{
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy hh:mm:ss a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))
        return formattedDate
    }
    fun getMacAddress(): String {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val mac = networkInterface.hardwareAddress
            if (mac != null && mac.isNotEmpty()) {
                val stringBuilder = StringBuilder()
                for (byte in mac) {
                    stringBuilder.append(String.format("%02X:", byte))
                }
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.deleteCharAt(stringBuilder.length - 1)
                }
                return stringBuilder.toString()
            }
        }
        return "02:00:00:00:00:00"

    }
    fun getIPAddress(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress

            if (ipAddress != 0) {
                // Convert IP address from integer to human-readable format
                return InetAddress.getByAddress(
                    byteArrayOf(
                        (ipAddress and 0xff).toByte(),
                        (ipAddress shr 8 and 0xff).toByte(),
                        (ipAddress shr 16 and 0xff).toByte(),
                        (ipAddress shr 24 and 0xff).toByte()
                    )
                ).hostAddress
            }
        } else {
            try {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                            // Ensure it's not a loopback address and IPv6
                            return address.hostAddress
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return "Not Available"
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun generateInfo(context: Context): Array<String?> {
        val infoArray = arrayOf(
            getOperatorName(context),
            getSignalStrength(context),
            getSNR(context),
            getNetworkType(context),
            getFrequencyBand(context),
            getCellID(context),
            getTimestamp(),
            getMacAddress(),
            getIPAddress(context)
        )
        return infoArray
    }




}
