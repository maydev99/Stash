package com.bombadu.stash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*

class Auth : AppCompatActivity() {

    private var isCreatingNewAccount = false
    //private var email: String? = null
    private var auth: FirebaseAuth? = null
    //private var password : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isCreatingNewAccount) {
                createNewAccount(email, password)
            } else {
                signIn(email, password)
            }
        }



        createAccountTextView.setOnClickListener {
            if (!isCreatingNewAccount) {
                titleTextView.text = getString(R.string.new_account)
                signInButton.text = getString(R.string.create_account)
                createAccountTextView.text = getString(R.string.sign_in)
                isCreatingNewAccount = true
            } else {
                titleTextView.text = getString(R.string.stash)
                signInButton.text = getString(R.string.sign_in)
                createAccountTextView.text = getString(R.string.create_account)
                isCreatingNewAccount = false
            }


        }


    }

    private fun signIn(email: String, password: String) {
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, StashList::class.java))
                    makeAToast("Sign_In Successful")
                    finish()
                } else {
                    makeAToast("Sign-In Failed")
                }
            }

    }

    private fun createNewAccount(email: String, password: String) {
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, StashList::class.java))
                    makeAToast("Account Created")
                    finish()
                } else {
                    makeAToast("Account Creation Failed")
                }
            }

    }

    private fun makeAToast(tMessage: String) {
        Toast.makeText(this, tMessage, Toast.LENGTH_SHORT).show()
    }
}
