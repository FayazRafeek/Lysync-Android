package com.fayazmohamed.lysync.Api

import com.fayazmohamed.lysync.Model.DataResp
import com.fayazmohamed.lysync.Model.PostResp
import com.fayazmohamed.lysync.Model.RegisterReq
import com.fayazmohamed.lysync.Model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface ApiEndpoints {

//    @Headers("Content-Type: application/x-www-form-urlencoded")
//    @POST("api/auth/register")
//    fun registerUser(@Field("name") name :String, @Field("email") email :String,@Field("password") password :String): Call<PostResp>

//    @Headers("Content-Type: application/x-www-form-urlencoded")

    @POST("auth/register")
    @FormUrlEncoded
    fun registerUser(@Field("name") name: String,@Field("email") email: String,@Field("password") password: String): Call<PostResp>

    @POST("auth/login")
    @FormUrlEncoded
    fun loginUser(@Field("email") email: String ,@Field("password") password: String): Call<PostResp>

    @POST("device/addDevice")
    @FormUrlEncoded
    fun addDevice(@Field("deviceId") deviceId: String ,@Field("type") type: String, @Field("fcmToken") fcmToken: String,): Call<PostResp>

    @DELETE("device/removeDevice/{deviceId}")
    fun removeDevice(@Path("deviceId") deviceId: String): Call<PostResp>

    @GET("data/getAllDatas")
    fun getAllData(): Call<List<DataResp>>

    @POST("data/addData")
    @FormUrlEncoded
    fun addData(@Field("data") data: String ): Call<PostResp>



    @Multipart
    @POST("file/uploadFile")
    fun uploadImage(
            @Part image: MultipartBody.Part,
            @Part("fileName") fileName : String,
            @Part("desc") desc: RequestBody
    ): Call<UploadResponse>

}