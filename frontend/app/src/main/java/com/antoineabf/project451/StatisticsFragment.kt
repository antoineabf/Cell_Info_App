package com.antoineabf.project451

import com.antoineabf.project451.api.CellDataService
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.antoineabf.project451.api.model.Statistics
import com.antoineabf.project451.api.model.infoForStat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class StatisticsFragment : Fragment() {
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private lateinit var buttonSendDatetime: Button
    private var connectivityTimePerOperatorTextView: TextView? = null
    private var connectivityTimePerNetworkTypeTextView: TextView? = null
    private var signalPowerPerNetworkTypeTextView: TextView? = null
    private var signalPowerPerDeviceTextView: TextView? = null
    private var SNRPerNetworkTypeTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        editTextStartDate = view.findViewById(R.id.editTextStartDate)
        editTextEndDate = view.findViewById(R.id.editTextEndDate)
        buttonSendDatetime = view.findViewById(R.id.buttonSendDatetime)
        connectivityTimePerOperatorTextView = view.findViewById(R.id.txtConnectivityTimePerOperator)
        connectivityTimePerNetworkTypeTextView = view.findViewById(R.id.txtConnectivityTimePerNetworkType)
        signalPowerPerNetworkTypeTextView = view.findViewById(R.id.txtSignalPowerPerNetworkType)
        signalPowerPerDeviceTextView = view.findViewById(R.id.txtSignalPowerPerDevice)
        SNRPerNetworkTypeTextView = view.findViewById(R.id.txtSNRPerNetworkType)

        editTextStartDate.setOnClickListener {
            showDateTimePicker(editTextStartDate)
        }

        editTextEndDate.setOnClickListener {
            showDateTimePicker(editTextEndDate)
        }

        buttonSendDatetime.setOnClickListener{
            val context: Context = requireContext()
            getStatistics(context)
        }

        return view
    }

    private fun showDateTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, selectedHour, selectedMinute ->
                        val datetime = Calendar.getInstance()
                        datetime.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                        val formattedDatetime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", datetime)
                        editText.setText(formattedDatetime)
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    fun getStatistics(context: Context){
        val info = infoForStat();
        val ip = CaptureInfo().getIPAddress(context);
        info.userIP = ip;
        info.start = editTextStartDate.text.toString();
        info.end = editTextEndDate.text.toString();
        CellDataService.CellDataApi().get_statistics(info).enqueue(object:
            Callback<Statistics>{
            override fun onResponse(call: Call<Statistics>, response: Response<Statistics>) {
                val statistics = response.body()
                if (statistics != null) {
                    // Display operator
                    val operatorText = statistics.operator?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" } ?: "Not Available"
                    connectivityTimePerOperatorTextView?.text = operatorText

                    // Display network type
                    val networkTypeText = statistics.networkType?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" } ?: "Not Available"
                    connectivityTimePerNetworkTypeTextView?.text = networkTypeText

                    // Display signal powers
                    val signalPowersText = statistics.signalPowers?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" } ?: "Not Available"
                    signalPowerPerNetworkTypeTextView?.text = signalPowersText

                    // Display signal power average
                    val signalPowerAvg = statistics.signalPowerAvg ?: "Not Available"
                    signalPowerPerDeviceTextView?.text = "$signalPowerAvg"

                    // Display SINR/SNR
                    val sinrSNRText = statistics.sinrSNR?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" } ?: "Not Available"
                    SNRPerNetworkTypeTextView?.text = sinrSNRText

                } else {
                    connectivityTimePerOperatorTextView?.text="Not Available"
                    connectivityTimePerNetworkTypeTextView?.text="Not Available"
                    signalPowerPerNetworkTypeTextView?.text="Not Available"
                    signalPowerPerDeviceTextView?.text ="Not Available"
                    SNRPerNetworkTypeTextView?.text = " Not Available"






                }
            }

            override fun onFailure(call: Call<Statistics>, t: Throwable) {
                return;
            }
        });

    }



}
