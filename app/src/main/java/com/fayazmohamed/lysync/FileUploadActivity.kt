package com.fayazmohamed.lysync

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fayazmohamed.lysync.Api.ApiClient
import com.fayazmohamed.lysync.Model.UploadRequestBody
import com.fayazmohamed.lysync.Model.UploadResponse
import com.fayazmohamed.lysync.databinding.ActivityFileUploadBinding
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class FileUploadActivity : AppCompatActivity() , UploadRequestBody.UploadCallback{


    lateinit var binding : ActivityFileUploadBinding
    lateinit var apiClient : ApiClient
    private var selectedImageUri: Uri? = null

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        apiClient = ApiClient()
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.filSelct.setOnClickListener {
            openFileChooser()
        }

        binding.uploadBtn.setOnClickListener {
            uploadFile()
        }
    }


    fun openFileChooser(){

        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    binding.selectedFile.setImageURI(selectedImageUri)
                }
            }
        }
    }


    fun View.snackbar(message: String) {
        Snackbar.make(
                this,
                message,
                Snackbar.LENGTH_LONG
        ).also { snackbar ->
            snackbar.setAction("Ok") {
                snackbar.dismiss()
            }
        }.show()
    }


    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }


    private val TAG = "DEBUG"

    fun uploadFile(){

        if (selectedImageUri == null) {
            binding.root.snackbar("Select an Image First")
            return
        }

        val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

        Log.d(TAG, "uploadFile: FILE DISCR =>  " + parcelFileDescriptor)

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        Log.d(TAG, "uploadFile: FILE => " + file)

        binding.uploadProgress.visibility  = View.VISIBLE
        binding.uploadProgress.progress = 0

        val body = UploadRequestBody(file, "image", this)

        Log.d(TAG, "uploadFile: BODY " + body.contentType())

        var reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val name: RequestBody = "file".toRequestBody("text/plain".toMediaTypeOrNull())

        apiClient.getApiService(this).uploadImage(MultipartBody.Part.createFormData(
                "file",
                file.name,
                reqBody
        ),"sample_file_name", "json".toRequestBody("multipart/form-data".toMediaTypeOrNull()))
                .enqueue(object : Callback<UploadResponse> {

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        Log.d(TAG, "onFailure: UPLOAD RROR => " + t)
                        binding.root.snackbar(t.message!!)
                        binding.uploadProgress.progress = 0
                    }

                    override fun onResponse(
                            call: Call<UploadResponse>,
                            response: Response<UploadResponse>
                    ) {
                        Log.d(TAG, "onFailure: UPLOAD SUCESS => " + response.body())
                        response.body()?.let {
                            binding.root.snackbar(it.message)
                            binding.uploadProgress.progress = 100
                        }
                    }

                })

//        apiClient.getApiService(this).uploadImage(
//                MultipartBody.Part.createFormData(
//                        "image",
//                        file.name,
//                        body
//                ),
//                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "json")
//        ).enqueue(object : Callback<UploadResponse> {
//            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
//                binding.root.snackbar(t.message!!)
//                binding.uploadProgress.progress = 0
//            }
////
//            override fun onResponse(
//                    call: Call<UploadResponse>,
//                    response: Response<UploadResponse>
//            ) {
//                response.body()?.let {
//                    binding.root.snackbar(it.message)
//                    binding.uploadProgress.progress = 100
//                }
//            }
//        })


    }

    override fun onProgressUpdate(percentage: Int) {

        binding.uploadProgress.progress = percentage
    }
}