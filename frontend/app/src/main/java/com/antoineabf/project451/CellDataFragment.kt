package com.antoineabf.project451

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity


class CellDataFragment : Fragment() {
    private var operatorTextView: TextView? = null
    private var signalPowerTextView: TextView? = null
    private var SNRTextView: TextView? = null
    private var networkTypeTextView: TextView? = null
    private var frequencyBandTextView: TextView? = null
    private var cellIDTextView: TextView? = null
    private var timestampTextView: TextView? = null
    private val PERMISSION_REQUEST_CODE = 123
    private val handler = Handler(Looper.getMainLooper())
    private val updateInfoRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun run() {
            // Update the TextViews with new information
            assignInfoToTextViews(requireView())

            // Schedule next execution after 10 seconds
            handler.postDelayed(this, 10 * 1000)
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start updating the fragment every 10 seconds
        handler.postDelayed(updateInfoRunnable, 10 * 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Remove the callbacks to prevent memory leaks
        handler.removeCallbacks(updateInfoRunnable)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
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
        val updateButton = view.findViewById<Button>(R.id.btnUpdateInfo)
        // Set OnClickListener for the button
        updateButton.setOnClickListener {
            // Call function to update text views
            updateInfo()
        }

        requestPermissions(activity)

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
    @RequiresApi(Build.VERSION_CODES.P)
    private fun updateInfo() {
        // Update text views with new information
        val infoArray = CaptureInfo().generateInfo(requireContext())
        operatorTextView?.text = infoArray[0]
        signalPowerTextView?.text = infoArray[1]
        SNRTextView?.text = infoArray[2]
        networkTypeTextView?.text = infoArray[3]
        frequencyBandTextView?.text = infoArray[4]
        cellIDTextView?.text = infoArray[5]
        timestampTextView?.text = infoArray[6]
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun requestPermissions(activity: FragmentActivity?) {
        if (activity != null && !hasPermissions(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun hasPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }





}