package com.antoineabf.project451

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CellDataFragment : Fragment() {
    private var operatorTextView: TextView? = null
    private var textViewSignalPower: TextView? = null
    private var textViewSINR: TextView? = null
    private var textViewNetworkType: TextView? = null
    private var textViewFrequencyBand: TextView? = null
    private var textViewCellID: TextView? = null
    private var textViewTimestamp: TextView? = null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cell_data, container, false)
        operatorTextView = view.findViewById(R.id.txtOperator)
        textViewCellID = view.findViewById(R.id.textViewCellID)
        textViewTimestamp = view.findViewById(R.id.textViewTimestamp)

        operatorTextView?.text = getOperatorName()
        return view
    }

    private fun getOperatorName(): String? {
        val manager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.networkOperatorName
    }
}