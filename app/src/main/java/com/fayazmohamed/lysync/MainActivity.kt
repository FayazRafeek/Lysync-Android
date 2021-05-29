package com.fayazmohamed.lysync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fayazmohamed.lysync.Api.ApiClient
import com.fayazmohamed.lysync.Model.DataResp
import com.fayazmohamed.lysync.Model.PostResp
import com.fayazmohamed.lysync.databinding.ActivityMainBinding
import com.fayazmohamed.lysync.databinding.DataListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var sessionManager: SessionManager
    lateinit var apiClient : ApiClient

    companion object {
        var IS_ACTIVITY_ALIVE : Boolean = false
        val REFETCH_TRIGGER: MutableLiveData<String> by lazy {
            MutableLiveData<String>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IS_ACTIVITY_ALIVE = true;

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutBtn.setOnClickListener {
            logout()
        }

        if(intent.getBooleanExtra("LOGIN", false))
            addDeviceReq()

        setUpRecyclerView()

        getAllData()
        binding.addBtn.setOnClickListener { addData() }

        REFETCH_TRIGGER.observe(this, androidx.lifecycle.Observer {
            if (it != null && it.equals("REFETCH")){
                getAllData()
                REFETCH_TRIGGER.postValue("")
            }

        })

        binding.fileUploadBtn.setOnClickListener{
            startActivity(Intent(this,FileUploadActivity::class.java))
        }
    }


    lateinit var adapter: MainAdapter
    fun setUpRecyclerView(){
        adapter = MainAdapter()
        binding.mainRecycler.layoutManager = LinearLayoutManager(this)
        binding.mainRecycler.adapter = adapter

        binding.swipeLayout.setOnRefreshListener { getAllData() }
    }

    fun addDeviceReq(){

        var divId = UUID.randomUUID().toString()
        sessionManager.fetchFcmToken()?.let {
            apiClient.getApiService(this).addDevice(deviceId = divId, type = "Android", fcmToken = it)
                    .enqueue(object : Callback<PostResp> {
                        override fun onFailure(call: Call<PostResp>, t: Throwable) {
                            Log.d("DEBUG", "onResponse:DEVICE ERROR Fail => " + t)
                        }

                        override fun onResponse(call: Call<PostResp>, response: Response<PostResp>) {
                            Log.d("DEBUG", "onResponse:DEVICE RESPONSE => " + response.body())
                            sessionManager.saveDeviceId(divId)
                        }
                    })
        }

    }

    private val TAG = "DEBUG"
    fun logout(){

        showProgress()
        sessionManager.fetchDeviceId()?.let {
            apiClient.getApiService(this).removeDevice(it)
                    .enqueue(object : Callback<PostResp> {
                        override fun onResponse(call: Call<PostResp>, response: Response<PostResp>) {
                            Log.d(TAG, "onResponse: DEvice remove success " + response.body())
                            hideProgress()
                            sessionManager.saveAuthToken("")
                            startAuthActivity()
                        }

                        override fun onFailure(call: Call<PostResp>, t: Throwable) {
                            Log.d(TAG, "onResponse: DEvice remove success " + t)
                            sessionManager.saveAuthToken("")
                            hideProgress()
                            startAuthActivity()
                        }
                    })
        }

    }

    fun getAllData(){

        showProgress()

        apiClient.getApiService(this).getAllData()
                .enqueue(object : Callback<List<DataResp>> {
                    override fun onResponse(call: Call<List<DataResp>>, response: Response<List<DataResp>>) {
                        Log.d(TAG, "onResponse: GOT ALL DATA " + response)
                        var body = response.body()
                        if (body != null) {
                            adapter.updateData(body)
                        }
                        hideProgress()
                    }

                    override fun onFailure(call: Call<List<DataResp>>, t: Throwable) {
                        Log.d(TAG, "onFailure: GET DATA FAILED => " + t)
                        hideProgress()
                    }
                })

    }

    fun addData(){

        var data = binding.addDataInp.text.toString()

        binding.addProgress.visibility = View.VISIBLE
        apiClient.getApiService(this).addData(data)
                .enqueue(object : Callback<PostResp> {
                    override fun onResponse(call: Call<PostResp>, response: Response<PostResp>) {

                        binding.addProgress.visibility = View.INVISIBLE

                        binding.addDataInp.setText("")
                        Log.d(TAG, "onResponse: GOT ALL DATA " + response)
                        var body = response.body()
                        if (body != null) {
                            if (body.status) {
                                toast("Data added successfully")
                                getAllData()
                            } else {
                                toast("Data added failed")
                            }
                        }
                    }

                    override fun onFailure(call: Call<PostResp>, t: Throwable) {
                        binding.addDataInp.setText("")
                        binding.addProgress.visibility = View.INVISIBLE
                        toast("Data failed to add")
                        Log.d(TAG, "onFailure: GET DATA FAILED => " + t)
                    }
                })

    }

    fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun startAuthActivity(){
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }


    fun showProgress() {
        (binding).swipeLayout.isRefreshing = true}
    fun hideProgress() {
        (binding).swipeLayout.isRefreshing = false}


    class MainAdapter() : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

        var dataList : ArrayList<DataResp> = ArrayList()

        inner class ViewHolder(val binding: DataListBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    DataListBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            var data = dataList.get(position)

            holder.binding.dataText.setText(data.value)

            var modifid= data.modified

            var calendar = Calendar.getInstance()
            calendar.timeInMillis = modifid * 1000L



            val sb = StringBuilder()
            sb.append(calendar.get(Calendar.DAY_OF_MONTH)).append("/")
                    .append(calendar.get(Calendar.MONTH))
                    .append(calendar.get(Calendar.YEAR))
            val c = sb.toString()

            holder.binding.dataText.setText(data.value)
            holder.binding.dataTime.setText(c)

        }

        fun updateData(list : List<DataResp>){
            dataList = ArrayList(list)
            notifyDataSetChanged()

        }
        override fun getItemCount(): Int {
            return dataList.size
        }
    }


    override fun onPause() {
        super.onPause()
        IS_ACTIVITY_ALIVE = false
    }

    override fun onResume() {
        super.onResume()
        IS_ACTIVITY_ALIVE = true
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_ACTIVITY_ALIVE = false
    }
}


