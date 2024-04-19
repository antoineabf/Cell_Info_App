package com.antoineabf.project451.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

class CellData {
    @SerializedName("operator")
    var operator: String? = null
    @SerializedName("signalPower")
    var signal_power: Float? = null
    @SerializedName("sinr_snr")
    var sinr_snr: Float? = null
    @SerializedName("networkType")
    var network_type: String? = null
    @SerializedName("frequency_band")
    var frequency_band: String? = null
    @SerializedName("cell_id")
    var cell_id: String? = null
    @SerializedName("timestamp")
    var timestamp: String? = null
    @SerializedName("user_ip")
    var user_ip:String? = null
    @SerializedName("user_mac")
    var user_mac:String?=null
}