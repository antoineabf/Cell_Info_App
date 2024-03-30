package com.antoineabf.project451

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import androidx.core.app.ActivityCompat
import java.lang.Math.pow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round



class CellDataFragment : Fragment() {
    private var operatorTextView: TextView? = null
    private var signalPowerTextView: TextView? = null
    private var SNRTextView: TextView? = null
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
        activity?.let {

            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123
            )
        }
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cell_data, container, false)
        assignInfoToTextViews(view)




        return view
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun assignInfoToTextViews(view: View) {
        operatorTextView = view.findViewById(R.id.txtOperator)
        signalPowerTextView = view.findViewById(R.id.txtSignalPower)
        SNRTextView = view.findViewById(R.id.txtSNR)
        networkTypeTextView = view.findViewById(R.id.txtNetworkType)
        frequencyBandTextView = view.findViewById(R.id.txtFrequencyBand)
        cellIDTextView = view.findViewById(R.id.txtCellID)
        timestampTextView = view.findViewById(R.id.txtTimestamp)
        val infoArray = CaptureInfo().generateInfo(requireContext())
        operatorTextView?.text = infoArray[0]
        signalPowerTextView?.text = infoArray[1]
        SNRTextView?.text = infoArray[2]
        networkTypeTextView?.text = infoArray[3]
        frequencyBandTextView?.text = infoArray[4]
        cellIDTextView?.text = infoArray[5]
        timestampTextView?.text = infoArray[6]
    }


}