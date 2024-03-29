package com.antoineabf.project451

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.antoineabf.project451.api.model.CellData


class CellDataFragment : Fragment() {
    private var operatorTextView: TextView? = null
    private var signalPowerTextView: TextView? = null
    private var SINRTextView: TextView? = null
    private var networkTypeTextView: TextView? = null
    private var frequencyBandTextView: TextView? = null
    private var cellIDTextView: TextView? = null
    private var timestampTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cell_data, container, false)
        operatorTextView = view.findViewById(R.id.txtOperator)
        signalPowerTextView = view.findViewById(R.id.txtSignalPower)
        SINRTextView = view.findViewById(R.id.txtSINR)
        networkTypeTextView = view.findViewById(R.id.txtNetworkType)
        frequencyBandTextView = view.findViewById(R.id.txtFrequencyBand)
        cellIDTextView = view.findViewById(R.id.txtCellID)
        timestampTextView = view.findViewById(R.id.txtTimestamp)

        val cellData: CellData? = null


        operatorTextView?.text = getOperatorName()
        networkTypeTextView?.text = getNetworkType(requireContext())

        return view
    }

    private fun getOperatorName(): String? {
        val manager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.networkOperatorName
    }

    private fun getNetworkType(context: Context): String {
        // from geeks4geeks

        // ConnectionManager instance
        val mConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mInfo = mConnectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if (mInfo == null || !mInfo.isConnected) return "-"

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
                else -> "?"
            }
        }
        return "?"
    }
//    private fun getCellID(): String {
//        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val cellLocation = telephonyManager.allCellInfo
//    }

}