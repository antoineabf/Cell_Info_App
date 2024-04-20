package com.antoineabf.project451.api.model

import com.google.gson.annotations.SerializedName

class infoForStat {

    @SerializedName("user_ip")
    var userIP: String? = null
    @SerializedName("start_date")
    var start: String? = null
    @SerializedName("end_date")
    var end: String? = null

}