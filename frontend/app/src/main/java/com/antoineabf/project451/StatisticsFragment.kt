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
import android.widget.Toast
import com.antoineabf.project451.api.Authentication
import com.antoineabf.project451.api.model.Statistics
import com.antoineabf.project451.api.model.infoForStat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
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
    ): View {
        val view: View
        if (Authentication.getToken() == "guest" ||Authentication.getToken() == null){
            view = inflater.inflate(R.layout.fragment_statistics_logged_out, container, false)
            }
        else{
            view = inflater.inflate(R.layout.fragment_statistics, container, false)
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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                //add 1 minute to it
                val updatedDate = Date(Date().time + 60000)
                //put the date in a string in the specified format
                val updatedDateStr = dateFormat.format(updatedDate)

                if (editTextEndDate.text?.isEmpty() == true || editTextStartDate.text?.isEmpty() == true){
                    Toast.makeText(context, "Please enter both dates", Toast.LENGTH_SHORT).show()
                    clearScreen()
                }
                else if (isDateGreaterThan(editTextStartDate.text.toString(),updatedDateStr) == true
                    || isDateGreaterThan(editTextEndDate.text.toString(),updatedDateStr) == true){
                    Toast.makeText(context, "We can't predict the future", Toast.LENGTH_SHORT).show()
                    clearScreen()
                }
                else if (isDateGreaterThan(editTextStartDate.text.toString(),editTextEndDate.text.toString()) == true){
                    Toast.makeText(context, "Start date can't be after end date", Toast.LENGTH_SHORT).show()
                    clearScreen()
                }
                else {
                    val context: Context = requireContext()
                    getStatistics(context)
                }
            }
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

    private fun isDateGreaterThan(date1: String, date2: String): Boolean? {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val parsedDate1: Date? = format.parse(date1)
        val parsedDate2: Date? = format.parse(date2)
        return parsedDate1?.after(parsedDate2)
    }

    fun getStatistics(context: Context){
        val info = infoForStat();
        val ip = CaptureInfo().getIPAddress(context);
        info.userIP = ip;
        info.start = editTextStartDate.text.toString();
        info.end = editTextEndDate.text.toString();
        Toast.makeText(context, "Getting statistics", Toast.LENGTH_SHORT).show()
        CellDataService.CellDataApi().get_statistics(info,
            "Bearer ${Authentication.getToken()}").enqueue(object:
            Callback<Statistics>{
            override fun onResponse(call: Call<Statistics>, response: Response<Statistics>) {
                val statistics = response.body()
                if (statistics != null) {

                    if (statistics.operator?.isEmpty() == true && statistics.networkType?.isEmpty() == true && statistics.signalPowers?.isEmpty() == true &&statistics.signalPowerAvg == 0f && statistics.sinrSNR?.isEmpty() == true){
                        Toast.makeText(context, "No statistics available in this period", Toast.LENGTH_SHORT).show()
                        clearScreen()
                    }
                        // Display operator
                    if (statistics.operator?.isNotEmpty() == true) {
                        val operatorText =
                            statistics.operator?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" }
                                ?: "Not Available"
                        connectivityTimePerOperatorTextView?.text = operatorText
                    }
                    else{
                        connectivityTimePerOperatorTextView?.text="Not Available"
                    }

                    // Display network type
                    if (statistics.networkType?.isNotEmpty() == true) {
                        val networkTypeText =
                            statistics.networkType?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" }
                                ?: "Not Available"
                        connectivityTimePerNetworkTypeTextView?.text = networkTypeText
                    }
                    else{
                        connectivityTimePerNetworkTypeTextView?.text="Not Available"
                    }

                    // Display signal powers
                    if (statistics.signalPowers?.isNotEmpty() == true) {
                        val signalPowersText =
                            statistics.signalPowers?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" }
                                ?: "Not Available"
                        signalPowerPerNetworkTypeTextView?.text = signalPowersText
                    }
                    else{
                        signalPowerPerNetworkTypeTextView?.text="Not Available"
                    }

                    // Display signal power average
                    if (statistics.signalPowerAvg?.toInt() != 0) {
                        val signalPowerAvg = statistics.signalPowerAvg ?: "Not Available"
                        signalPowerPerDeviceTextView?.text = "$signalPowerAvg"
                    }
                    else{
                        signalPowerPerDeviceTextView?.text="Not Available"
                    }

                    // Display SINR/SNR
                    if(statistics.sinrSNR?.isNotEmpty() == true) {
                        val sinrSNRText =
                            statistics.sinrSNR?.entries?.joinToString(separator = "\n") { "${it.key}:${it.value}" }
                                ?: "Not Available"
                        SNRPerNetworkTypeTextView?.text = sinrSNRText
                    }
                    else{
                        SNRPerNetworkTypeTextView?.text ="Not Available"
                    }

                } else {
                    connectivityTimePerOperatorTextView?.text="Not Available"
                    connectivityTimePerNetworkTypeTextView?.text="Not Available"
                    signalPowerPerNetworkTypeTextView?.text="Not Available"
                    signalPowerPerDeviceTextView?.text ="Not Available"
                    SNRPerNetworkTypeTextView?.text = " Not Available"
                    Toast.makeText(context, "No statistics available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Statistics>, t: Throwable) {
                return;
            }
        });

    }

    private fun clearScreen(){
        connectivityTimePerOperatorTextView?.text = "Not Available"
        connectivityTimePerNetworkTypeTextView?.text = "Not Available"
        signalPowerPerNetworkTypeTextView?.text = "Not Available"
        signalPowerPerDeviceTextView?.text = "Not Available"
        SNRPerNetworkTypeTextView?.text = "Not Available"
    }


}
