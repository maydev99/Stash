package com.bombadu.stash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*

class Auth : AppCompatActivity() {

    private var isCreatingNewAccount = false
    private var auth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateForm(email, password)) {
                return@setOnClickListener
            }

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

    private fun validateForm(email: String, password: String): Boolean {

        var valid = true

        if (TextUtils.isEmpty(email)) {
            emailEditText.error = getString(R.string.required)
            valid = false
        } else {
            emailEditText.error = null
        }

        if (TextUtils.isEmpty(password)){
            passwordEditText.error = getString(R.string.required)
            valid = false
        } else {
            passwordEditText.error = null
        }

        return  !valid

    }

    private fun signIn(email: String, password: String) {
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, StashList::class.java))
                    makeAToast("Sign-In Successful")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sign_in_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.password_reset) {
            startActivity(Intent(this, PasswordReset::class.java))
        }

        return super.onOptionsItemSelected(item)
    }
}
