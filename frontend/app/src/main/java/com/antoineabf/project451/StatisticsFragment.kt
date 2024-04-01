package com.antoineabf.project451

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

        buttonSendDatetime.setOnClickListener {
            val startDate = editTextStartDate.text.toString()
            val endDate = editTextEndDate.text.toString()
            println("Start Date: $startDate, End Date: $endDate")
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


}
