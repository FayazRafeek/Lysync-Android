package com.fayazmohamed.lysync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fayazmohamed.lysync.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class GoogleProfileActivity : AppCompatActivity() {


    lateinit var binding : ActivityProfileBinding

    lateinit var gso : GoogleSignInOptions
    lateinit var mGoogleSignInClient : GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.button.setOnClickListener { signOut() }
    }


    override fun onStart() {
        super.onStart()

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto: Uri? = acct.photoUrl

            binding.name.setText(personName)
            binding.profileImage.setImageURI(personPhoto)
            binding.email.setText(personEmail)
            binding.uId.setText(personId)
        } else
            Log.d("DBUG", "onStart: ACCOUNT NULL")
    }


    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                startActivity(Intent(this,AuthActivity::class.java))
                finish()
            }
    }
}