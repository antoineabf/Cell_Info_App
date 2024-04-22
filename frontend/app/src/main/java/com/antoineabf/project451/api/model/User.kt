package com.antoineabf.project451.api.model
import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("user_name")
    var username: String? = null
    @SerializedName("password")
    var password: String? = null
}