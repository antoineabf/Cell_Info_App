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
import java.lang.Math.pow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

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
                return "Not available"
            }
        return "Not available"


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
                            return "Not available"
                        }
                        is CellInfoWcdma -> {
                            // For 3G (UMTS/WCDMA)
                            return "Not available"
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
                return "Not available"
            }
        }
        return "Not available"
    }
    fun getNetworkType(context: Context): String {
        // from geeks4geeks

        // ConnectionManager instance
        val mConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mInfo = mConnectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if (mInfo == null || !mInfo.isConnected) return "not available"

        // If Connected to Wifi
        if (mInfo.type == ConnectivityManager.TYPE_WIFI) return "WIFI"

        // If Connected to Mobile
        if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return when (mInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN,
                TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "not available"
            }
        }
        return "not available"
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
            return "Not available"
        }
        return "Not available"
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
                        else -> "Not available" // Handle other cell types or unknown cases
                    }
                }
            }
        } else {

            return "Not available" // Default value if cell ID cannot be retrieved
        }
        return "not available"// Default value if cell ID cannot be retrieved
    }
    fun getTimestamp(): String{
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))
        return formattedDate
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
            getTimestamp()
        )
        return infoArray
    }




}
