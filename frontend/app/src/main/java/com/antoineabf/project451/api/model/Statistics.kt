package com.antoineabf.project451.api.model

import com.google.gson.annotations.SerializedName

class Statistics {

    @SerializedName("operators")
    var operator: Map<String, Float>? = null

    @SerializedName("network_types")
    var networkType: Map<String, Float>? = null

    @SerializedName("signal_powers")
    var signalPowers: Map<String, Float>? = null

    @SerializedName("signal_power_avg_device")
    var signalPowerAvg: Float? = null

    @SerializedName("sinr_snr")
    var sinrSNR: Map<String, Float>? = null
}