package com.fayazmohamed.lysync.Model

import com.google.gson.annotations.SerializedName

data class RegisterReq(

    @SerializedName("name")
    var name: String,
    @SerializedName("email")
    var email: String,
    @SerializedName("password")
    var password: String
)
