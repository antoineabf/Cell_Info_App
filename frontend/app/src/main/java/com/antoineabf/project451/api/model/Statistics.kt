package com.antoineabf.project451.api.model

import com.google.gson.annotations.SerializedName

class Statistics {

    @SerializedName("operators")
    var operator: String? = null
    @SerializedName("network_types")
    var networkType: String? = null
    @SerializedName("signal_powers")
    var signalPower: Float? = null
    @SerializedName("signal_power_avg_device")
    var signalPowerAvg: Float? = null
    @SerializedName("sinr_snr")
    var sinrSNR: Float? = null
}