package com.fayazmohamed.lysync.Model

import com.google.gson.annotations.SerializedName

data class DataResp(
        @SerializedName("value")
        var value : String,
        @SerializedName("modified")
        var modified : Long
)
