package com.fayazmohamed.lysync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fayazmohamed.lysync.Api.ApiClient
import com.fayazmohamed.lysync.Model.PostResp
import com.fayazmohamed.lysync.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AuthActivity : AppCompatActivity(){

    lateinit var binding : ActivityAuthBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


        apiClient = ApiClient()
        sessionManager = SessionManager(this)


        binding.registerBtn.setOnClickListener {
            var name = binding.nameInp.text.toString()
            var email = binding.emailInp.text.toString()
            var pass = binding.passInp.text.toString()
            sendRegisterRequest(name, email, pass);
            showProgress()
        }

        binding.loginBtn.setOnClickListener {
            var email = binding.emailInp.text.toString()
            var pass = binding.passInp.text.toString()
            loginRequest(email, pass);
            showProgress()
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("845438271296-ojslhuuns67itfu9hnj18nasq1nkpggg.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.signInButton.setOnClickListener{
            signIn()
        }

    }

    companion object {
        var RC_SIGN_IN : Int = 101
    }
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    lateinit var mGoogleSignInClient : GoogleSignInClient

    fun showProgress() { binding.authProgress.visibility = View.VISIBLE}
    fun hiderogress() { binding.authProgress.visibility = View.INVISIBLE}

    fun sendRegisterRequest(name: String, email: String, pass: String){

            apiClient.getApiService(this).registerUser(name = name, email = email, password = pass)
                .enqueue(object : Callback<PostResp> {
                    override fun onFailure(call: Call<PostResp>, t: Throwable) {
                        Log.d("DEBUG", "onResponse: ERROR Fail => " + t)
                        hiderogress()
                    }

                    override fun onResponse(call: Call<PostResp>, response: Response<PostResp>) {
                        val loginResponse = response.body()

                        hiderogress()
                        Log.d("DEBUG", "onResponse: RESPONSE => " + response.body())
                        if (loginResponse!!.status) {
                            var token = loginResponse.token
                            sessionManager.saveAuthToken(token)
                            startMainActivity()
                            Log.d("DEBUG", "onResponse: TOKEN => " + token)
                        } else {
                            Log.d("DEBUG", "onResponse: ERROR => " + response.errorBody())
                        }
                    }
                })


    }


    fun loginRequest(email: String, pass: String){

        apiClient.getApiService(this).loginUser(email = email, password = pass)
            .enqueue(object : Callback<PostResp> {
                override fun onFailure(call: Call<PostResp>, t: Throwable) {
                    hiderogress()
                    Log.d("DEBUG", "onResponse: ERROR Fail => " + t)
                }

                override fun onResponse(call: Call<PostResp>, response: Response<PostResp>) {
                    val loginResponse = response.body()
                    hiderogress()
                    Log.d("DEBUG", "onResponse: RESPONSE => " + response.body())
                    if (loginResponse!!.status) {
                        var token = loginResponse.token
                        sessionManager.saveAuthToken(token)
                        startMainActivity()
                        Log.d("DEBUG", "onResponse: TOKEN => " + token)
                    } else {
                        Log.d("DEBUG", "onResponse: ERROR => " + response.errorBody())
                    }
                }
            })


    }

    fun startMainActivity(){
        startActivity(Intent(this, MainActivity::class.java).putExtra("LOGIN", true))
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)


            handleSignInResult(task)
        }
    }

    private  val TAG = "DEBUG"
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account!!.idToken
            Log.d(TAG, "handleSignInResult: TOKEN => " + idToken)
            startProfile()
        } catch (e: ApiException) {
            Log.d(TAG, "handleSignInResult: AUTH RROR :> " + e)
        }
    }

    fun startProfile(){
        startActivity(Intent(this, GoogleProfileActivity::class.java))
        finish()
    }

}