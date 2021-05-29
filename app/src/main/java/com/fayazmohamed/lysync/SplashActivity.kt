package com.fayazmohamed.lysync

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fayazmohamed.lysync.databinding.ActivitySplashBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class SplashActivity : AppCompatActivity() {

    lateinit var binding : ActivitySplashBinding
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        sessionManager.fetchAuthToken()?.let {

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("DEBUG", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                sessionManager.saveFcmToken(token+"")

                Handler(Looper.myLooper()!!).postDelayed(
                        Runnable {
                            if(it != null && !it.equals("")){
                                startActivity(Intent(this,MainActivity::class.java))
                                finish()
                            } else {
                                startActivity(Intent(this,AuthActivity::class.java))
                                finish()
                            }
                        } , 1500
                )
            })
        }

    }
}