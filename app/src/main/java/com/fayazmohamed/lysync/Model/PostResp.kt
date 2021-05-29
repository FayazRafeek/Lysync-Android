package com.fayazmohamed.lysync.Model

import com.google.gson.annotations.SerializedName

data class PostResp(

    @SerializedName("status")
    var status: Boolean,

    @SerializedName("token")
    var token: String,

)
